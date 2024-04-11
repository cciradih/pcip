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
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Main {
    public static void main(String[] args) throws IOException {
        //  设置首选 IP 的延迟，会过滤延迟高于此数值的 IP。
        int timeout = 200;

        //  设置线程数，默认使用 CPU 核心数 * 延迟。
        int nThreads = Runtime.getRuntime().availableProcessors() * timeout;

        //  获取 IP 段文件。存放在 src/main/resources/ips-v4，来源 https://www.cloudflare-cn.com/ips-v4。
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("ips-v4");
        Objects.requireNonNull(inputStream);
        List<String> cidrNotationList;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            cidrNotationList = bufferedReader.lines().toList();
        }
        System.out.println("CIDR notation list size: " + cidrNotationList.size());

        //  获取所有可用的 IP 地址。
        List<String> addressesList = cidrNotationList.stream()
                .flatMap(cidrNotation -> Arrays.stream(new SubnetUtils(cidrNotation).getInfo().getAllAddresses()))
                .toList();
        DecimalFormat decimalFormat = new DecimalFormat("#,###");
        System.out.println("Addresses list size: " + decimalFormat.format(addressesList.size()));

        //  使用线程池。
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<Future<Result>> resultFutureList = addressesList.stream()
                .map(addresses -> executorService.submit(new Ping(addresses, timeout)))
                .toList();
        executorService.shutdown();
        List<Result> resultList = new ArrayList<>();
        DecimalFormat percentageFormat = new DecimalFormat("00.00%");
        List<Result> resultList1 = resultFutureList.stream()
                .map(resultFuture -> {
                    Result result = null;
                    try {
                        result = resultFuture.get();
                    } catch (InterruptedException | ExecutionException ignored) {
                    }
                    resultList.add(result);
                    double percentage = BigDecimal.valueOf(resultList.size())
                            .divide(BigDecimal.valueOf(addressesList.size()), 4, RoundingMode.HALF_UP)
                            .doubleValue();
                    System.out.print("Processed quantity: " + decimalFormat.format(resultList.size()) + ", current progress: " + percentageFormat.format(percentage) + "\r");
                    return result;
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(Result::getMillisecond))
                .toList();

        //  输出延迟最低的 5 个 IP。
        resultList1.stream().limit(5).forEach(result -> System.out.println("Address: " + result.getAddress() + ", millisecond: " + result.getMillisecond() + "ms."));

        //  拆分结果，XLS 支持 65_536 行和 256 列，而 XLSX 支持 1_048_576 行和 16_384 列。
        List<List<Result>> partitionResultList = ListUtils.partition(resultList1, 1_000_000);

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
                    cell.setCellValue(resultList2.get(i).getMillisecond());
                }
            }

            try (FileOutputStream outputStream = new FileOutputStream("result.xlsx")) {
                workbook.write(outputStream);
            }
        }
    }
}
