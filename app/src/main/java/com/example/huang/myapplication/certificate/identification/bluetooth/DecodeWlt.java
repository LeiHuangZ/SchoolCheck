package com.example.huang.myapplication.certificate.identification.bluetooth;
public class DecodeWlt
{
  static
  {
    try
    {
      System.loadLibrary("DecodeWlt");
    } catch (UnsatisfiedLinkError e) {
      e.printStackTrace();
    }
  }

  public native int Wlt2Bmp(String paramString1, String paramString2);
}