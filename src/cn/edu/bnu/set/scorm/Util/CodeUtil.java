package cn.edu.bnu.set.scorm.Util;

import java.util.Random;

import android.util.Base64;

public class CodeUtil {

	// ��������ַ���������ָ���ַ�������
	public static String getEight(int length) {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int num = random.nextInt(base.length());
			sb.append(base.charAt(num));
		}
		return sb.toString();
	}

	//���ܲ����ַ���ǰ��������ַ�����������ʾ�������ļ�λ��
	public static byte [] base64_encode(byte [] buffer) throws Exception {
		byte [] code = Base64.encode(buffer, Base64.DEFAULT);
		code = (getEight(8) + new String(code)).getBytes();
		return code;
	}

	//���룺�Ƚ����ܺ���ַ���ǰ8λ�ַ�ȥ���������ֱ��ʾ�����Ŀ������ļ�λ��
	public static String base64_decode(String base64Code)
			throws Exception {
		byte [] buffer = base64Code.substring(8).getBytes();
		buffer = Base64.decode(buffer, Base64.DEFAULT);
		return (new String(buffer));
	}
}