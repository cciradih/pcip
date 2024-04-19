package org.eu.cciradih.pcip;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException {
        //  获取 IP 段文件，来源 https://www.cloudflare-cn.com/ips-v4。
        InputStream inputStream = (InputStream) new URI("https://www.cloudflare-cn.com/ips-v4").toURL()
                .getContent();
        List<String> cidrNotationList = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .toList();
        System.out.println("CIDR notation list size: " + cidrNotationList.size());

        //  设置首选 IP 的延迟，默认 200，会过滤延迟高于此数值的 IP。
        int timeout = 200;

        //  获取所有可用的 IP 地址。
        List<String> addressList = cidrNotationList.stream()
                .flatMap(cidrNotation -> Arrays.stream(new SubnetUtils(cidrNotation)
                        .getInfo()
                        .getAllAddresses()))
                .toList();
        int addressListSize = addressList.size();
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        System.out.println("Address list size: " + decimalFormat.format(addressListSize));

        //  使用虚拟线程。
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
        List<Future<Result>> futureList = addressList.stream()
                .map(address -> executorService.submit(new Connect(address, timeout)))
                .toList();

        //  开启基于 CPU 核心数的信号量限流，获取结果集。
        Semaphore semaphore = new Semaphore(Runtime.getRuntime().availableProcessors());
        AtomicLong processed = new AtomicLong();
        processed.set(0L);
        DecimalFormat percentageFormat = new DecimalFormat("00.00%");
        List<Result> resultList = futureList.stream()
                .map(resultFuture -> {
                    try {
                        semaphore.acquire();
                        Result result = resultFuture.get();
                        semaphore.release();

                        long processedSize = processed.get();
                        processed.set(processedSize + 1);

                        double percentage = BigDecimal.valueOf(processedSize)
                                .divide(BigDecimal.valueOf(addressListSize), 4, RoundingMode.HALF_UP)
                                .doubleValue();

                        System.out.print("Processed quantity: " + decimalFormat.format(processedSize) +
                                ", current progress: " + percentageFormat.format(percentage) + "\r");
                        return result;
                    } catch (InterruptedException | ExecutionException ignored) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Result::getMillisecond))
                .toList();
        executorService.close();

        //  输出延迟最低的 5 个 IP。
        resultList.stream()
                .limit(5)
                .forEach(result -> System.out.println("Address: " + result.getAddress() + ", millisecond: " +
                        result.getMillisecond() + "ms."));

        //  拆分结果，每 1_000_000 一张表，XLS 支持 65_536 行和 256 列，XLSX 支持 1_048_576 行和 16_384 列。
        List<List<Result>> partitionResultList = ListUtils.partition(resultList, 1_000_000);

        //  生成 Excel 结果。
        try (Workbook workbook = new XSSFWorkbook()) {
            for (int i = 0; i < partitionResultList.size(); i++) {
                Sheet sheet = workbook.createSheet("Address" + i);
                sheet.setDefaultColumnWidth(15);

                Row headerRow = sheet.createRow(0);

                Cell headerCell = headerRow.createCell(0);
                headerCell.setCellValue("Address");
                headerCell = headerRow.createCell(1);
                headerCell.setCellValue("Millisecond");
                List<Result> resultList2 = partitionResultList.get(i);

                for (int j = 0; j < resultList2.size(); j++) {
                    Row row = sheet.createRow(j + 1);

                    Cell cell = row.createCell(0);
                    cell.setCellValue(resultList2.get(j).getAddress());
                    cell = row.createCell(1);
                    cell.setCellValue(resultList2.get(j).getMillisecond());
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream("result.xlsx")) {
                workbook.write(outputStream);
            }
        }
    }
}
