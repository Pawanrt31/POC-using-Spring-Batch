//package com.philips.poc.step;
//
//import org.apache.poi.*;
//import org.apache.poi.poifs.filesystem.POIFSFileSystem;
//import org.apache.poi.ss.usermodel.Row;
//import org.apache.poi.ss.usermodel.Workbook;
//import org.apache.poi.ss.usermodel.WorkbookFactory;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.batch.item.ParseException;
//import org.springframework.batch.item.ReaderNotOpenException;
//import org.springframework.batch.item.ResourceAware;
//import org.springframework.batch.item.support.AbstractItemCountingItemStreamItemReader;
//import org.springframework.beans.factory.InitializingBean;
//import org.springframework.core.io.Resource;
//import org.springframework.util.Assert;
//
//import java.io.*;
//
//
///**
// * <p>
// * This {@link AbstractItemCountingItemStreamItemReader} is capable of reading one sheet of one Excel file,
// * row per row, and mapping each row to a Java bean via a {@link RowMapper}.
// * This implementation make usage of Apache POI core framework to read either OLE2 or OOXML format (detection is
// * based on file content and not on file extension).
// * </p>
// * <p>
// * If you keep default settings, you only need to specify a
// * {@link org.springframework.core.io.Resource} and a {@link RowMapper}.</p>
// * <p>
// * Note that the restartability of this reader is only based on
// * {@link AbstractItemCountingItemStreamItemReader}.
// * </p>
// *
// * @param <T> return type of a {@link #read()}
// * @author Almex
// * @see #setRowsToSkip(int)
// * @see #setSheetIndex(int)
// * @implSpec This implementation is not thread-safe
// * @since 1.1
// */
//public class ExcelSheetItemReader<T> extends AbstractItemCountingItemStreamItemReader<T> implements
//        IndexedResourceAwareItemStreamReader<T>,
//        InitializingBean,
//        ResourceAwareItemReaderItemStream<T>,
//        ResourceAwareItemStreamReader<T> {
//
//    private static Logger LOGGER = LoggerFactory.getLogger(ExcelSheetItemReader.class);
//    private RowMapper<? extends T> rowMapper;
//    private Resource resource;
//    private Workbook workbook;
//    private boolean noInput = false;
//    private int rowsToSkip = 0;
//    private int sheetIndex = 0;
//
//    /**
//     * Validate if the {@link java.io.File} is of a supported format (i.e.: OLE2 or OOXML).
//     *
//     * @param file targeting the Excel workbook to validate.
//     * @return <code>true</code> if the format is supported, <code>false</code> otherwise.
//     * @throws IOException if an I/O error occurs.
//     */
//    private static boolean isValidExcelFile(File file) throws IOException {
//        try (InputStream inputStream = new PushbackInputStream(new FileInputStream(file), 8)) {
//            return POIFSFileSystem.hasPOIFSHeader(inputStream) || POIXMLDocument.hasOOXMLHeader(inputStream);
//        }
//    }
//
//    /**
//     * Checking that the returned row is not null should validate if we have not reached the End Of File.
//     *
//     * @param row the {@link Row} from which we check the reference
//     * @return {@code true} if the reference to the {@code row} is {@code null}, {@code false} otherwise.
//     */
//    private static boolean isEof(final Row row) {
//        return row == null;
//    }
//
//    @Override
//    public void afterPropertiesSet() throws Exception {
//        Assert.notNull(rowMapper, "rowMapper is required");
//    }
//
//    @Override
//    protected T doRead() throws Exception {
//        T result;
//
//        Row row = readRow();
//
//        if (!isEof(row)) {
//            try {
//                result = rowMapper.mapRow(row, getCurrentIndex());
//            } catch (Exception ex) {
//                throw new ParseException("Parsing error at line: " + getCurrentIndex() + " in resource=["
//                        + resource.getDescription() + "]", ex);
//            }
//        } else {
//            noInput = true;
//            //-- In order to full-fil ItemReader contract we must return null
//            result = null;
//        }
//
//        return result;
//    }
//
//    /**
//     * @return next line (skipping the number of row specified by {@link #setRowsToSkip(int)}.
//     */
//    private Row readRow() {
//        Row result;
//
//        if (workbook == null) {
//            throw new ReaderNotOpenException("Reader must be open before it can be read.");
//        }
//
//        result = workbook.getSheetAt(sheetIndex).getRow(getCurrentIndex());
//
//        if (result == null) {
//            noInput = true;
//        }
//
//        return result;
//    }
//
//    @Override
//    protected void doOpen() throws Exception {
//        Assert.notNull(resource, "Input resource must be set");
//
//        noInput = true;
//        if (!resource.exists()) {
//            LOGGER.warn("Input resource does not exist {}", resource.getDescription());
//            return;
//        }
//
//        if (!isValidExcelFile(resource.getFile())) {
//            LOGGER.warn("Input resource is neither an OLE2 file, nor an OOXML file {}", resource.getDescription());
//            return;
//        }
//
//        /**
//         * ATTENTION: if we use the resource.getFileInputStream() the stream is never released!
//         * So, we create our own FileInputStream instead. Don't know why. Seems like a bug in Apache POI
//         */
//        try (InputStream inputStream = new FileInputStream(resource.getFile())) {
//            this.workbook = WorkbookFactory.create(inputStream);
//        } finally {
//            if (workbook != null) {
//                workbook.close();
//            }
//        }
//
//        noInput = false;
//        jumpToItem(0);
//    }
//
//    @Override
//    protected void doClose() throws Exception {
//        this.noInput = true;
//    }
//
//    @Override
//    public T read() throws Exception {
//        T result = null;
//
//        if (!noInput) {
//            result = super.read();
//            if (result == null) {
//                noInput = true;
//            }
//        }
//
//        return result;
//    }
//
//    @Override
//    public int getCurrentIndex() {
//        int result;
//
//        if (isEof()) {
//            result = -1;
//        } else {
//            // it's a zero-based index ()
//            result = getCurrentItemCount() + rowsToSkip - 1;
//        }
//
//        return result;
//    }
//
//    /**
//     * @return true if we have reached the end of file false otherwise
//     */
//    public boolean isEof() {
//        return noInput;
//    }
//
//    @Override
//    public void setResource(Resource resource) {
//        this.resource = resource;
//    }
//
//    /**
//     * Set the {@link RowMapper} used to map a {@link org.apache.poi.ss.usermodel.Row}
//     * to your <code>T</code> type.
//     *
//     * @param rowMapper an implementation of {@link RowMapper}
//     */
//    public void setRowMapper(RowMapper<? extends T> rowMapper) {
//        this.rowMapper = rowMapper;
//    }
//
//    /**
//     * By default this value is initialized to 0.
//     *
//     * @param rowsToSkip set the number of the first row to read -1.
//     */
//    public void setRowsToSkip(int rowsToSkip) {
//        this.rowsToSkip = rowsToSkip;
//    }
//
//    /**
//     * By default this value is initialized to 0.
//     *
//     * @param sheetIndex set the zero-indexed based sheet.
//     */
//    public void setSheetIndex(int sheetIndex) {
//        this.sheetIndex = sheetIndex;
//    }
//}