package example.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by YS-GZD-1495 on 2018/2/7.
 */
public class MD5Util {

    private static String byteArrayToHexString(byte b[]) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++)
            resultSb.append(byteToHexString(b[i]));

        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0)
            n += 256;
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static String MD5Encode(String origin, String charsetname) {
        String resultString = null;
        try {
            resultString = new String(origin);
            MessageDigest md = MessageDigest.getInstance("MD5");
            if (charsetname == null || "".equals(charsetname))
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes()));
            else
                resultString = byteArrayToHexString(md.digest(resultString
                        .getBytes(charsetname)));
        } catch (Exception exception) {
        }
        return resultString;
    }

    private static final String hexDigits[] = { "0", "1", "2", "3", "4", "5",
            "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };
    public static String md5Password(String password) {

        try {
            // 得到一个信息摘要器
            MessageDigest digest = MessageDigest.getInstance("md5");
            byte[] result = digest.digest(password.getBytes());
            StringBuffer buffer = new StringBuffer();
            // 把每一个byte 做一个与运算 0xff;
            for (byte b : result) {
                // 与运算
                int number = b & 0xff;// 加盐
                String str = Integer.toHexString(number);
                if (str.length() == 1) {
                    buffer.append("0");
                }
                buffer.append(str);
            }

            // 标准的md5加密后的结果
            return buffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }

    }

    public static void main(String[] args) {
//        System.out.println(md5Password("appid=wx583a69e73cea3868&nonceStr=c9dd22afe7474774a8bcad403ccb743b&package=prepay_id=wx201802121154410996554db70054543887&signType=MD5&timeStamp=1518407682&key=4cdc1725775c47c784beabf5f01e8295"));
        String temp1="appId=wx583a69e73cea3868&nonceStr=67f6a31915fe4edba9d6eac40e4ab993&package=prepay_id=wx201802121204474c9a069ef50014878558&signType=MD5&timeStamp=1518408287&key=4cdc1725775c47c784beabf5f01e8295";
        String temp2="appid=wx583a69e73cea3868&nonceStr=67f6a31915fe4edba9d6eac40e4ab993&package=prepay_id=wx201802121204474c9a069ef50014878558&signType=MD5&timeStamp=1518408287&key=4cdc1725775c47c784beabf5f01e8295";
        for (int i=0;i<temp1.length();i++){
            if (temp1.charAt(i)!=temp2.charAt(i)){
                System.out.println(i+" "+temp1.charAt(i)+" "+temp2.charAt(i));
            }
        }
    }
}
