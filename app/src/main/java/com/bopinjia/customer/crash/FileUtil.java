package com.bopinjia.customer.crash;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.os.Environment;
import android.text.TextUtils;

/**
 * <h3>File������</h3>
 * <p>��Ҫ��װ��һЩ���ļ���д�Ĳ���
 * 
 */
public final class FileUtil {
    
    private FileUtil() {
        throw new Error("���n��");
    }

    /** �ָ���. */
    public final static String FILE_EXTENSION_SEPARATOR = ".";

    /**"/"*/
    public final static String SEP = File.separator;

    /** SD����Ŀ¼ */
    public static final String SDPATH = Environment
            .getExternalStorageDirectory() + File.separator;

    /**
     * �ж�SD���Ƿ����
     * @return SD�����÷���true
     */
    public static boolean hasSdcard() {
        String status = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(status);
    }

    /**
     * ��ȡ�ļ�������
     * <br>
     * Ĭ��utf-8����
     * @param filePath �ļ�·��
     * @return �ַ���
     * @throws IOException 
     */
    public static String readFile(String filePath) throws IOException {
        return readFile(filePath, "utf-8");
    }

    /**
     * ��ȡ�ļ�������
     * @param filePath �ļ�Ŀ¼
     * @param charsetName �ַ�����
     * @return String�ַ���
     */
    public static String readFile(String filePath, String charsetName)
            throws IOException {
        if (TextUtils.isEmpty(filePath))
            return null;
        if (TextUtils.isEmpty(charsetName))
            charsetName = "utf-8";
        File file = new File(filePath);
        StringBuilder fileContent = new StringBuilder("");
        if (file == null || !file.isFile())
            return null;
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            return fileContent.toString();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ��ȡ�ı��ļ���List�ַ���������(Ĭ��utf-8����)
     * @param filePath �ļ�Ŀ¼
     * @return �ļ������ڷ���null�����򷵻��ַ�������
     * @throws IOException 
     */
    public static List<String> readFileToList(String filePath)
            throws IOException {
        return readFileToList(filePath, "utf-8");
    }

    /**
     * ��ȡ�ı��ļ���List�ַ���������
     * @param filePath �ļ�Ŀ¼
     * @param charsetName �ַ�����
     * @return �ļ������ڷ���null�����򷵻��ַ�������
     */
    public static List<String> readFileToList(String filePath,
            String charsetName) throws IOException {
        if (TextUtils.isEmpty(filePath))
            return null;
        if (TextUtils.isEmpty(charsetName))
            charsetName = "utf-8";
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (file == null || !file.isFile()) {
            return null;
        }
        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(
                    file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            return fileContent;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * ���ļ���д������
     * @param filePath �ļ�Ŀ¼
     * @param content Ҫд�������
     * @param append ���Ϊ true��������д���ļ�ĩβ����������д���ļ���ʼ��
     * @return д��ɹ�����true�� д��ʧ�ܷ���false
     * @throws IOException 
     */
    public static boolean writeFile(String filePath, String content,
            boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath))
            return false;
        if (TextUtils.isEmpty(content))
            return false;
        FileWriter fileWriter = null;
        try {
            createFile(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.flush();
            return true;
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    

    /**
     * ���ļ���д������<br>
     * Ĭ�����ļ���ʼ������д������
     * @param filePath �ļ�Ŀ¼
     * @param stream �ֽ�������
     * @return д��ɹ�����true�����򷵻�false
     * @throws IOException 
     */
    public static boolean writeFile(String filePath, InputStream stream)
            throws IOException {
        return writeFile(filePath, stream, false);
    }

    /**
     * ���ļ���д������
     * @param filePath �ļ�Ŀ¼
     * @param stream �ֽ�������
     * @param append ���Ϊ true��������д���ļ�ĩβ����
     *              Ϊfalseʱ�����ԭ�������ݣ���ͷ��ʼд
     * @return д��ɹ�����true�����򷵻�false
     * @throws IOException 
     */
    public static boolean writeFile(String filePath, InputStream stream,
            boolean append) throws IOException {
        if (TextUtils.isEmpty(filePath))
            throw new NullPointerException("filePath is Empty");
        if (stream == null)
            throw new NullPointerException("InputStream is null");
        return writeFile(new File(filePath), stream,
                append);
    }

    /**
     * ���ļ���д������
     * Ĭ�����ļ���ʼ������д������
     * @param file ָ���ļ�
     * @param stream �ֽ�������
     * @return д��ɹ�����true�����򷵻�false
     * @throws IOException 
     */
    public static boolean writeFile(File file, InputStream stream)
            throws IOException {
        return writeFile(file, stream, false);
    }

    /**
     * ���ļ���д������
     * @param file ָ���ļ�
     * @param stream �ֽ�������
     * @param append Ϊtrueʱ�����ļ���ʼ������д�����ݣ�
     *              Ϊfalseʱ�����ԭ�������ݣ���ͷ��ʼд
     * @return д��ɹ�����true�����򷵻�false
     * @throws IOException 
     */
    public static boolean writeFile(File file, InputStream stream,
            boolean append) throws IOException {
        if (file == null)
            throw new NullPointerException("file = null");
        OutputStream out = null;
        try {
            createFile(file.getAbsolutePath());
            out = new FileOutputStream(file, append);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                out.write(data, 0, length);
            }
            out.flush();
            return true;
        } finally {
            if (out != null) {
                try {
                    out.close();
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * �����ļ�
     * @param sourceFilePath Դ�ļ�Ŀ¼��Ҫ���Ƶ��ļ�Ŀ¼��
     * @param destFilePath Ŀ���ļ�Ŀ¼�����ƺ���ļ�Ŀ¼��
     * @return �����ļ��ɹ�����true�����򷵻�false
     * @throws IOException 
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath)
            throws IOException {
        InputStream inputStream = null;
        inputStream = new FileInputStream(sourceFilePath);
        return writeFile(destFilePath, inputStream);
    }
    

    /**
     * ��ȡĳ��Ŀ¼�µ��ļ���
     * @param dirPath Ŀ¼
     * @param fileFilter ������
     * @return ĳ��Ŀ¼�µ������ļ���
     */
    public static List<String> getFileNameList(String dirPath,
            FilenameFilter fileFilter) {
        if (fileFilter == null)
            return getFileNameList(dirPath);
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        
        File[] files = dir.listFiles(fileFilter);
        if (files == null) 
            return Collections.emptyList();
        
        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    /**
     * ��ȡĳ��Ŀ¼�µ��ļ���
     * @param dirPath Ŀ¼
     * @return ĳ��Ŀ¼�µ������ļ���
     */
    public static List<String> getFileNameList(String dirPath) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles();
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    /**
     * ��ȡĳ��Ŀ¼�µ�ָ����չ�����ļ�����
     * @param dirPath Ŀ¼
     * @return ĳ��Ŀ¼�µ������ļ���
     */
    public static List<String> getFileNameList(String dirPath,
            final String extension) {
        if (TextUtils.isEmpty(dirPath))
            return Collections.emptyList();
        File dir = new File(dirPath);
        File[] files = dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String filename) {
                if (filename.indexOf("." + extension) > 0)
                    return true;
                return false;
            }
        });
        if (files == null)
            return Collections.emptyList();
        List<String> conList = new ArrayList<String>();
        for (File file : files) {
            if (file.isFile())
                conList.add(file.getName());
        }
        return conList;
    }

    /**
     * ����ļ�����չ��
     * @param filePath �ļ�·��
     * @return ���û����չ��������""
     */
    public static String getFileExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (extenPosi == -1) {
            return "";
        }
        return (filePosi >= extenPosi) ? "" : filePath.substring(extenPosi + 1);
    }

    /**
     * �����ļ�
     * @param path �ļ��ľ���·��
     * @return
     */
    public static boolean createFile(String path) {
        if (TextUtils.isEmpty(path))
            return false;
        return createFile(new File(path));
    }

    /**
     * �����ļ�
     * @param file
     * @return �����ɹ�����true
     */
    public static boolean createFile(File file) {
        if (file == null || !makeDirs(getFolderName(file.getAbsolutePath())))
            return false;
        if (!file.exists())
            try {
                return file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        return false;
    }

    /**
     * ����Ŀ¼�������Ƕ����
     * @param filePath Ŀ¼·��
     * @return  ���·��Ϊ��ʱ������false�����Ŀ¼�����ɹ����򷵻�true�����򷵻�false
     */
    public static boolean makeDirs(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File folder = new File(filePath);
        return (folder.exists() && folder.isDirectory()) ? true : folder
                .mkdirs();
    }

    /**
     * ����Ŀ¼�������Ƕ����
     * @param dir Ŀ¼
     * @return ���Ŀ¼�����ɹ����򷵻�true�����򷵻�false
     */
    public static boolean makeDirs(File dir) {
        if (dir == null)
            return false;
        return (dir.exists() && dir.isDirectory()) ? true : dir.mkdirs();
    }

    /**
     * �ж��ļ��Ƿ����
     * @param filePath �ļ�·��
     * @return ���·��Ϊ�ջ���Ϊ�հ��ַ������ͷ���false������ļ����ڣ������ļ���
     *          �ͷ���true����������ļ����߲����ڣ��򷵻�false
     */
    public static boolean isFileExist(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return false;
        }
        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * ��ò�����չ�����ļ�����
     * @param filePath �ļ�·��
     * @return
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0,
                    extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1,
                extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * ����ļ���
     * @param filePath �ļ�·��
     * @return ���·��Ϊ�ջ�մ�������·��������Ϊ��ʱ�������ļ���
     */
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * �������Ŀ¼����
     * @param filePath �ļ��ľ���·��
     * @return ���·��Ϊ�ջ�մ�������·��������Ϊ��ʱ�����Ϊ��Ŀ¼������"";
     *          ������Ǹ�Ŀ¼����������Ŀ¼���ƣ���ʽ�磺C:/Windows/Boot
     */
    public static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * �ж�Ŀ¼�Ƿ����
     * @param directoryPathĿ¼·��
     * @return ���·��Ϊ�ջ�հ��ַ���������false�����Ŀ¼�����ң�ȷʵ��Ŀ¼�ļ��У�
     *          ����true����������ļ��л��߲����ڣ��򷵻�false
     */
    public static boolean isFolderExist(String directoryPath) {
        if (TextUtils.isEmpty(directoryPath)) {
            return false;
        }
        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * ɾ��ָ���ļ���ָ��Ŀ¼�ڵ������ļ�
     * @param path �ļ���Ŀ¼�ľ���·��
     * @return ·��Ϊ�ջ�հ��ַ���������true���ļ������ڣ�����true���ļ�ɾ������true��
     *          �ļ�ɾ���쳣����false
     */
    public static boolean deleteFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return true;
        }
        return deleteFile(new File(path));
    }
    
    /**
     * ɾ��ָ���ļ���ָ��Ŀ¼�ڵ������ļ�
     * @param file
     * @return ·��Ϊ�ջ�հ��ַ���������true���ļ������ڣ�����true���ļ�ɾ������true��
     *          �ļ�ɾ���쳣����false
     */
    public static boolean deleteFile(File file) {
        if (file == null)
            throw new NullPointerException("file is null");
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        
        File[] files = file.listFiles();
        if (files == null)
            return true;
        for (File f : files) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * ɾ��ָ��Ŀ¼���ض����ļ�
     * @param dir
     * @param filter
     */
    public static void delete(String dir, FilenameFilter filter) {
        if (TextUtils.isEmpty(dir))
            return;
        File file = new File(dir);
        if (!file.exists())
            return;
        if (file.isFile())
            file.delete();
        if (!file.isDirectory())
            return;

        File[] lists = null;
        if (filter != null)
            lists = file.listFiles(filter);
        else
            lists = file.listFiles();

        if (lists == null)
            return;
        for (File f : lists) {
            if (f.isFile()) {
                f.delete();
            }
        }
    }

    /**
     * ����ļ����ļ��еĴ�С
     * @param path �ļ���Ŀ¼�ľ���·��
     * @return ���ص�ǰĿ¼�Ĵ�С ��ע�����ļ������ڣ�Ϊ�գ�����Ϊ�հ��ַ��������� -1
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }

   
}
