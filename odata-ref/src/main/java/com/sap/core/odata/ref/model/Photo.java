package com.sap.core.odata.ref.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class Photo {
  private static int counter = 1;
  private static byte[] defaultImage;

  private int id;
  private String name;
  private String type = "image/jpeg";
  private String imageUrl = "http://localhost/darth.jpg";
  private byte[] image = defaultImage;
  private byte[] binaryData;
  private String content;

  public Photo() {}

  public Photo(String name) {
    this();
    id = counter++;
    this.setName(name);
  }

  static {
    try {
      InputStream in = Photo.class.getResourceAsStream("/darth.jpg");

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      int c = 0;
      while ((c = in.read()) != -1) {
        bos.write((char) c);
      }

      Photo.defaultImage = bos.toByteArray();
    } catch (IOException e) {
      throw new ModelException(e);
    }
  }

  public int getId() {
    return id;
  }

  public void setName(String name) {
    this.name = name;

  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getImageUri() {
    return imageUrl;
  }

  public void setImageUri(String uri) {
    imageUrl = uri;

  }

  public byte[] getImage() {
    return image.clone();
  }

  public void setImage(byte[] image) {
    this.image = image;
  }

  public void setBinaryData(byte[] binaryData) {
    this.binaryData = binaryData;

  }

  public byte[] getBinaryData() {
    if (binaryData == null) {
      return null;
    } else {
      return binaryData.clone();
    }
  }

  public void setContent(final String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public static void reset() {
    counter = 1;
  }

  @Override
  public int hashCode() {
    return id;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Photo other = (Photo) obj;
    if (id != other.id)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "{\"Id\":" + id + ","
        + "\"Name\":\"" + name + "\","
        + "\"Type\":\"" + type + "\","
        + "\"ImageUrl\":\"" + imageUrl + "\","
        + "\"Image\":\"" + Arrays.toString(image) + "\","
        + "\"BinaryData\":\"" + Arrays.toString(binaryData) + "\"}";
  }
}
