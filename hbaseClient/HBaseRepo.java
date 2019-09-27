package com.mycompany.server.repo.hbase;

import com.google.common.collect.Maps;
import com.mycompany.server.utils.HBaseFilterUtil;
import lombok.extern.log4j.Log4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

@Repository
@Log4j
public class HBaseRepo {
    private final Connection connection;

    @Autowired
    public HBaseRepo(Connection hBaseConnection) {
        this.connection = hBaseConnection;
    }

    private static PrefixFilter getPrefixMatchFilter(String prefix) {
        return new PrefixFilter(Bytes.toBytes(prefix));
    }


    private static RowFilter getExactlyMatchRowFilter(String key) {

        return new RowFilter(CompareFilter.CompareOp.EQUAL, new RegexStringComparator("^" + key.replace("|", "\\|") + "$"));

    }

    public static void main(String[] args) {
        String rowkey = "372802196701270000";
    }

    @Nullable
    public byte[] get(String key, String tableName) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(key));
            Result result = table.get(get);
            return result.getValue(Bytes.toBytes("uc"), Bytes.toBytes("value"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 扫描 rowkey 前缀 key 的所有数据
     *
     * @param tableName
     * @param key
     * @return
     */
    public ArrayList<Map<String, Object>> scanWithExactlyMatchFilter(String tableName, String key) {
        System.out.println("scan exactly " + key);
        return scanWithFilter(tableName, key, getExactlyMatchRowFilter(key));
    }

    /**
     * 扫描 rowkey 前缀为 prefix 的所有数据
     *
     * @param tableName
     * @param prefix
     * @return
     */
    public ArrayList<Map<String, Object>> scanWithPrefixMatchFilter(String tableName, String prefix) {
        System.out.println("scan prefix " + prefix);
        return scanWithFilter(tableName, prefix, getPrefixMatchFilter(prefix));
    }

    private ArrayList<Map<String, Object>> scanWithFilter(String tableName, String startRow, FilterBase filterBase) {
        ArrayList<Map<String, Object>> resultsList = null;
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan(Bytes.toBytes(startRow));
            scan.setFilter(filterBase);
            ResultScanner resultScanner = table.getScanner(scan);
            resultsList = new ArrayList<>();
            for (Result result : resultScanner) {
                Map<String, Object> map = new HashMap<>();
                map.put("rowkey", Bytes.toString(result.getRow()));
                for (Cell cell : result.rawCells()) {
                    byte[] family = CellUtil.cloneFamily(cell);
                    byte[] qualifier = CellUtil.cloneQualifier(cell);
                    map.put(Bytes.toString(qualifier), Bytes.toString(result.getValue(family, qualifier)));
                }
                resultsList.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultsList;
    }

    /**
     * @param key       RowKey
     * @param tableName HBase表名
     * @return map对象
     * @throws IOException 异常
     */
    public Map<String, String> getByRowKey(String key, String tableName) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(key));
            Result result = table.get(get);
            return resultToMap(result);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * @param tableName      表名
     * @param family         列族名
     * @param queryQualifier 条件列
     * @param findQualifier  查询列
     * @param value          条件值
     * @return String的List对象
     * @throws IOException 异常
     */
    public List<String> scanBySingleColumn(String tableName, String family, String queryQualifier, String findQualifier, String value) throws IOException {
        List<String> list = new ArrayList<>();
        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();
        Filter filter = new SingleColumnValueFilter(Bytes
                .toBytes(family), Bytes.toBytes(queryQualifier), CompareFilter.CompareOp.EQUAL, Bytes
                .toBytes(value));
        scan.setFilter(filter);
        try {
            ResultScanner resultScanner = table.getScanner(scan);
            Iterator<Result> resultIterator = resultScanner.iterator();
            resultIterator.forEachRemaining(result -> {
                byte[] v = result.getValue(Bytes.toBytes(family), Bytes.toBytes(findQualifier));
                list.add(Bytes.toString(v));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * like模糊查询
     * @param tableName
     * @param family
     * @param col  列
     * @param sub  字符串
     * @return
     * @throws IOException
     */
    public  List<Map<String, String>> scanSubStringLikeColumn(String tableName, String family, String col, String sub) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            Filter filter = HBaseFilterUtil.subStringFilter(family, col, sub);
            scan.setFilter(filter);
            ResultScanner resultScanner = table.getScanner(scan);
            Iterator<Result> resultIterator = resultScanner.iterator();
            resultIterator.forEachRemaining(result -> list.add(resultToMap(result)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        }
        return list;
    }

    /**
     * 查询全表数据
     * @param tableName
     * @return
     * @throws IOException
     */
    public List<Map<String, String>> scanAllData(String tableName) throws IOException {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.setCaching(5000);
            ResultScanner resultScanner = table.getScanner(scan);
            Iterator<Result> resultIterator = resultScanner.iterator();
            resultIterator.forEachRemaining(result -> list.add(resultToMap(result)));
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException();
        }
        return list;
    }

    /**
     * @param tableName HBase表名
     * @param prefixKey 前缀RowKey
     * @param startKey
     * @param stopKey
     * @return Map的list对象
     * @throws IOException 异常
     */
    public List<Map<String, String>> scanByRowKeyPrefix(String tableName, String prefixKey, String startKey, String stopKey) {
        List<Map<String, String>> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            scan.setFilter(new RowFilter(CompareFilter.CompareOp.EQUAL, new BinaryPrefixComparator(Bytes.toBytes(prefixKey))));
            scan.setStartRow(Bytes.toBytes(startKey));
            scan.setStopRow(Bytes.toBytes(stopKey));
            ResultScanner resultScanner = table.getScanner(scan);
            Iterator<Result> resultIterator = resultScanner.iterator();
            resultIterator.forEachRemaining(result -> list.add(resultToMap(result)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    /**
     * 将HBase 行转map对象
     *
     * @param result HBase行
     * @return map对象
     */
    private Map<String, String> resultToMap(Result result) {
        Map<String, String> map = Maps.newHashMap();
        if (result.getRow() == null) {
            return null;
        }
        Cell[] cells = result.rawCells();
        map.put("rowKey", Bytes.toString(result.getRow()));
        if (cells[0] != null) {
            map.put("timestamp", String.valueOf(cells[0].getTimestamp()));
        }
        for (Cell cell : cells) {
            String value = new String(CellUtil.cloneValue(cell));
            if (value.equals("NULL") || value.equals("null")) {
                value = StringUtils.EMPTY;
            }
            map.put(new String(CellUtil.cloneQualifier(cell)), value);
        }
        return map;
    }

    /**
     * 保存一行记录到hBase，只支持一个列簇
     *
     * @param saveMap            保存的数据map
     * @param tableName          表名
     * @param familyName         列簇名
     * @param rowKey             rowKey
     * @param isDeleteExistTable 是否删除已经存在的表
     */
    public void save(Map<String, String> saveMap, String tableName, String familyName, String rowKey, boolean isDeleteExistTable) {
        try {
            Admin admin = connection.getAdmin();
            TableName tn = TableName.valueOf(tableName);
            if (isDeleteExistTable) {
                if (admin.tableExists(tn)) {
                    admin.disableTable(tn);
                    admin.deleteTable(tn);
                }
                createHBaseTable(admin, tableName, familyName);
            } else {
                if (!admin.tableExists(tn)) {
                    createHBaseTable(admin, tableName, familyName);
                }
            }
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            saveMap.forEach((k, v) ->
                    put.addColumn(Bytes.toBytes(familyName), Bytes.toBytes(k), Bytes.toBytes(v)));
            table.put(put);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    private void createHBaseTable(Admin admin, String tableName, String familyName) {
        TableName tn = TableName.valueOf(tableName);
        HTableDescriptor tableDescriptor = new HTableDescriptor(new HTableDescriptor(tn));
        tableDescriptor.addFamily(new HColumnDescriptor(familyName));
        try {
            admin.createTable(tableDescriptor);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    public List<Map<String, String>> scanAllByParamAndCaseCategory(String tableName, String family, String col1,
                                                                   String param, String col2, String caseCategory)
            throws IOException{
        List<Map<String, String>> list = new ArrayList<>();
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();
            List<Filter> filters = new ArrayList<>();
            Filter filter1 = HBaseFilterUtil.subStringFilter(family, col1, param);
            Filter filter2 = HBaseFilterUtil.subStringFilter(family, col2, caseCategory);
            filters.add(filter1);
            filters.add(filter2);
            Filter filterResutl = HBaseFilterUtil.andFilter(filters);
            scan.setFilter(filterResutl);
            ResultScanner resultScanner = table.getScanner(scan);
            Iterator<Result> resultIterator = resultScanner.iterator();
            resultIterator.forEachRemaining(result -> list.add(resultToMap(result)));
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException();
        }
        return list;
    }

//    public long rowCount(String tableName) throws Exception {
//        long rowCount = 0;
//        try {
//            Table table = connection.getTable(TableName.valueOf(tableName));
//            Scan scan = new Scan();
//            scan.setFilter(new FirstKeyOnlyFilter());
//            ResultScanner resultScanner = table.getScanner(scan);
//            for (Result result : resultScanner) {
//                rowCount += result.size();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new Exception();
//        }
//        return rowCount;
//    }
}
