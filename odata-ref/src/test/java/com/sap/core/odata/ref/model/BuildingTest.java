package com.sap.core.odata.ref.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.sap.core.odata.testutil.fit.BaseTest;

/**
 * @author SAP AG
 */
public class BuildingTest extends BaseTest {

  private static final String VALUE_NAME = "bd01";
  private static final String VALUE_IMAGE = "String for convert";

  @Test
  public void testId() {
    Building build1 = new Building(1, null);
    assertNotNull(build1.getId());
  }

  @Test
  public void testName() {
    Building build1 = new Building(1, VALUE_NAME);
    assertEquals(VALUE_NAME, build1.getName());
  }

  @Test
  public void testImage() {
    Building build1 = new Building(1, null);
    final byte[] byteArray = VALUE_IMAGE.getBytes();
    build1.setImage(byteArray);
    byte[] testArray = build1.getImage();
    assertEquals(byteArray.length, testArray.length);
    assertArrayEquals(byteArray, testArray);
  }

  @Test
  public void testRooms() {
    List<Room> list = Arrays.asList(new Room(1, null), new Room(2, null), new Room(3, null));
    Building building1 = new Building(1, null);
    building1.getRooms().add(list.get(0));
    building1.getRooms().add(list.get(1));
    building1.getRooms().add(list.get(2));
    assertEquals(list, building1.getRooms());
  }

}
