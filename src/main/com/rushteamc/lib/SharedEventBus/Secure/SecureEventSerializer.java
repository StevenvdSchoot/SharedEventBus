package com.rushteamc.lib.SharedEventBus.Secure;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class SecureEventSerializer
{
	private static final int KEYSIZE = 128; // If you are American you are allowed to upgrade this to 256 :(
	private final Map<String, SecretKey> keyRing = new HashMap<String, SecretKey>();
	private static final byte[] salt;
	
	static
	{
		switch(KEYSIZE)
		{
		case 8:
			byte[] s8   = {(byte) 0xE9};
			salt = s8;
			break;
		case 16:
			byte[] s16  = {(byte) 0xE9, (byte) 0xA4 };
			salt = s16;
			break;
		case 32:
			byte[] s32  = {(byte) 0xE9, (byte) 0xA4, (byte) 0x9A, (byte) 0x43 };
			salt = s32;
			break;
		case 64:
			byte[] s64  = {(byte) 0xE9, (byte) 0xA4, (byte) 0x9A, (byte) 0x43, (byte) 0x3D, (byte) 0xED, (byte) 0x29, (byte) 0x02 };
			salt = s64;
			break;
		case 128:
			byte[] s128 = {(byte) 0xE9, (byte) 0xA4, (byte) 0x9A, (byte) 0x43, (byte) 0x3D, (byte) 0xED, (byte) 0x29, (byte) 0x02, (byte) 0xFB, (byte) 0x69, (byte) 0x62, (byte) 0xE7, (byte) 0xC1, (byte) 0xCA, (byte) 0x31, (byte) 0xF3 };
			salt = s128;
			break;
		case 256:
			byte[] s256 = {(byte) 0xE9, (byte) 0xA4, (byte) 0x9A, (byte) 0x43, (byte) 0x3D, (byte) 0xED, (byte) 0x29, (byte) 0x02, (byte) 0xFB, (byte) 0x69, (byte) 0x62, (byte) 0xE7, (byte) 0xC1, (byte) 0xCA, (byte) 0x31, (byte) 0xF3, (byte) 0xDC, (byte) 0xD3, (byte) 0xC1, (byte) 0x23, (byte) 0x1A, (byte) 0x21, (byte) 0x9D, (byte) 0x58, (byte) 0xAD, (byte) 0x31, (byte) 0x29, (byte) 0x3A, (byte) 0xE0, (byte) 0xE9, (byte) 0xDF, (byte) 0xA7 };
			salt = s256;
			break;
		default:
			byte[] s    = {(byte) 0xE9, (byte) 0xA4, (byte) 0x9A, (byte) 0x43, (byte) 0x3D, (byte) 0xED, (byte) 0x29, (byte) 0x02, (byte) 0xFB, (byte) 0x69, (byte) 0x62, (byte) 0xE7, (byte) 0xC1, (byte) 0xCA, (byte) 0x31, (byte) 0xF3, (byte) 0xDC, (byte) 0xD3, (byte) 0xC1, (byte) 0x23, (byte) 0x1A, (byte) 0x21, (byte) 0x9D, (byte) 0x58, (byte) 0xAD, (byte) 0x31, (byte) 0x29, (byte) 0x3A, (byte) 0xE0, (byte) 0xE9, (byte) 0xDF, (byte) 0xA7, (byte) 0x0B, (byte) 0xAF, (byte) 0x7A, (byte) 0x58, (byte) 0xD0, (byte) 0x67, (byte) 0xA5, (byte) 0xDC, (byte) 0xE8, (byte) 0x20, (byte) 0xE5, (byte) 0x80, (byte) 0x17, (byte) 0x80, (byte) 0x9F, (byte) 0x49, (byte) 0x84, (byte) 0xD8, (byte) 0x1C, (byte) 0x4F, (byte) 0xEB, (byte) 0x99, (byte) 0x4B, (byte) 0x10, (byte) 0xDF, (byte) 0x6D, (byte) 0x42, (byte) 0x3D, (byte) 0x29, (byte) 0x9C, (byte) 0x8E, (byte) 0x4F, (byte) 0x82, (byte) 0x11, (byte) 0xC0, (byte) 0xE3, (byte) 0xDC, (byte) 0xA8, (byte) 0xEF, (byte) 0xAC, (byte) 0xB2, (byte) 0x38, (byte) 0x7F, (byte) 0xD6, (byte) 0x67, (byte) 0x32, (byte) 0x7B, (byte) 0xBC, (byte) 0xE0, (byte) 0xAA, (byte) 0x3B, (byte) 0x20, (byte) 0x89, (byte) 0xC5, (byte) 0x53, (byte) 0xBF, (byte) 0xF5, (byte) 0x61, (byte) 0xA4, (byte) 0x34, (byte) 0x02, (byte) 0x53, (byte) 0x1F, (byte) 0x08, (byte) 0x36, (byte) 0x8B, (byte) 0x61, (byte) 0x3C, (byte) 0x3A, (byte) 0xDA, (byte) 0x56, (byte) 0x37, (byte) 0x0E, (byte) 0x7F, (byte) 0x3F, (byte) 0xB8, (byte) 0x31, (byte) 0x7B, (byte) 0x4F, (byte) 0xD1, (byte) 0xA1, (byte) 0x09, (byte) 0xF8, (byte) 0xDA, (byte) 0x51, (byte) 0xF7, (byte) 0x7E, (byte) 0x3B, (byte) 0x0C, (byte) 0xED, (byte) 0x2E, (byte) 0xED, (byte) 0xDC, (byte) 0x82, (byte) 0xDE, (byte) 0x5B, (byte) 0x14, (byte) 0x57, (byte) 0x46, (byte) 0xF1, (byte) 0x2A, (byte) 0xEE, (byte) 0x74, (byte) 0x03, (byte) 0x98, (byte) 0xF8, (byte) 0xAC, (byte) 0xC6, (byte) 0x18, (byte) 0x7B, (byte) 0xF3, (byte) 0xC5, (byte) 0xEC, (byte) 0x5F, (byte) 0x10, (byte) 0xB2, (byte) 0xFE, (byte) 0xA0, (byte) 0xD6, (byte) 0x37, (byte) 0x26, (byte) 0xC9, (byte) 0x05, (byte) 0x43, (byte) 0xF9, (byte) 0x99, (byte) 0x72, (byte) 0x15, (byte) 0xA1, (byte) 0x5D, (byte) 0x3D, (byte) 0x68, (byte) 0xA2, (byte) 0x1C, (byte) 0x01, (byte) 0xB4, (byte) 0xE9, (byte) 0x73, (byte) 0x35, (byte) 0xED, (byte) 0x36, (byte) 0xC8, (byte) 0x3B, (byte) 0x86, (byte) 0xBE, (byte) 0x72, (byte) 0x0C, (byte) 0x5C, (byte) 0xB6, (byte) 0x2F, (byte) 0x9C, (byte) 0xFF, (byte) 0x39, (byte) 0x80, (byte) 0xCC, (byte) 0xCB, (byte) 0xC4, (byte) 0x8F, (byte) 0xE8, (byte) 0xB1, (byte) 0x68, (byte) 0xFD, (byte) 0xCE, (byte) 0xA0, (byte) 0x3C, (byte) 0x15, (byte) 0xC1, (byte) 0x71, (byte) 0x7E, (byte) 0xFA, (byte) 0x25, (byte) 0x44, (byte) 0x2F, (byte) 0x71, (byte) 0x18, (byte) 0x9B, (byte) 0x83, (byte) 0x03, (byte) 0x29, (byte) 0x58, (byte) 0xEA, (byte) 0xD8, (byte) 0xD6, (byte) 0x7E, (byte) 0x4F, (byte) 0x75, (byte) 0x0B, (byte) 0x37, (byte) 0x86, (byte) 0xA7, (byte) 0x82, (byte) 0x43, (byte) 0xA1, (byte) 0x3C, (byte) 0xA1, (byte) 0x29, (byte) 0xEB, (byte) 0x32, (byte) 0x8B, (byte) 0xD6, (byte) 0x7A, (byte) 0x44, (byte) 0x2D, (byte) 0xFA, (byte) 0x77, (byte) 0xD6, (byte) 0x64, (byte) 0x9B, (byte) 0x79, (byte) 0x8C, (byte) 0xDD, (byte) 0x80, (byte) 0xE5, (byte) 0x1D, (byte) 0xE0, (byte) 0xF3, (byte) 0x06, (byte) 0x62, (byte) 0xFF, (byte) 0xA6, (byte) 0x27, (byte) 0xE3, (byte) 0xAC, (byte) 0x8D};
			salt = s;
		}
	}
	
	public void setup()
	{
		;
	}
	
	public SecureEventMessage serialize(SecureEvent secureEvent) throws IOException
	{
		SecretKey key = keyRing.get(secureEvent.getGroup());
		if(key == null)
			throw new IllegalArgumentException("Could not find given group.");
		
		Serializable event = secureEvent.getEvent();
		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
		
		objectStream.writeObject(event);
		byte[] output = byteStream.toByteArray();
		
		objectStream.close();
		byteStream.close();
		
		return encrypt(secureEvent.getGroup(), key, output);
	}

	public SecureEvent deserialize(SecureEventMessage secureEventMessage) throws IllegalArgumentException, ClassCastException, IOException, ClassNotFoundException
	{
		SecretKey key = keyRing.get(secureEventMessage.getGroup());
		if(key == null)
			throw new IllegalArgumentException("Could not find group " + secureEventMessage.getGroup());
		
		byte[] data = decrypt(key, secureEventMessage);

		ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
		ObjectInputStream objectStream = new ObjectInputStream(byteStream);
		
		Object obj = objectStream.readObject();
		if( !(obj instanceof Serializable) )
			throw new ClassCastException("Could not cast deserialized object to Serialize type!");
		
		Serializable event = (Serializable) obj;
		
		objectStream.close();
		byteStream.close();
		
		return new SecureEvent(secureEventMessage.getGroup(), event);
	}
	
	public SecureEventMessage encrypt(String group, SecretKey key, byte[] input)
	{
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			cipher.init(Cipher.ENCRYPT_MODE, key);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		}
		
		AlgorithmParameters params = cipher.getParameters();
		
		byte[] iv;
		byte[] data;
		
		try {
			iv = params.getParameterSpec(IvParameterSpec.class).getIV();
		} catch (InvalidParameterSpecException e) {
			e.printStackTrace();
			return null;
		}
		
		
		try {
			data = cipher.doFinal(input);
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}

		return new SecureEventMessage(group, iv, data);
	}

	public byte[] decrypt(SecretKey key, SecureEventMessage input)
	{
		Cipher cipher;
		try {
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
			return null;
		}
		
		try {
			cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(input.getIv()));
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}
		
		byte[] output;
		
		try {
			output = cipher.doFinal(input.getData());
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			return null;
		} catch (BadPaddingException e) {
			e.printStackTrace();
			return null;
		}

		return output;
	}
	
	public void addGroup(String group, char[] password) throws NoSuchAlgorithmException, InvalidKeySpecException
	{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(password, salt, 65536, KEYSIZE);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKey secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
		
		keyRing.put(group, secretKey);
	}
	
	public void removeGroup(String group)
	{
		keyRing.remove(group);
	}
}
