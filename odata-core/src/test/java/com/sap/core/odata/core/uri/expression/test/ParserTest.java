package com.sap.core.odata.core.uri.expression.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeFacade;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.uri.expression.CommonExpression;
import com.sap.core.odata.api.uri.expression.ExpressionKind;
import com.sap.core.odata.api.uri.expression.ExpressionException;
import com.sap.core.odata.api.uri.expression.ExpressionVisitor;
import com.sap.core.odata.api.uri.expression.FilterExpression;
import com.sap.core.odata.core.edm.Bit;
import com.sap.core.odata.core.edm.EdmBinary;
import com.sap.core.odata.core.edm.EdmBoolean;
import com.sap.core.odata.core.edm.EdmByte;
import com.sap.core.odata.core.edm.EdmDateTime;
import com.sap.core.odata.core.edm.EdmDateTimeOffset;
import com.sap.core.odata.core.edm.EdmDecimal;
import com.sap.core.odata.core.edm.EdmDouble;
import com.sap.core.odata.core.edm.EdmGuid;
import com.sap.core.odata.core.edm.EdmInt16;
import com.sap.core.odata.core.edm.EdmInt32;
import com.sap.core.odata.core.edm.EdmInt64;
import com.sap.core.odata.core.edm.EdmSByte;
import com.sap.core.odata.core.edm.EdmSimpleTypeFacadeImpl;
import com.sap.core.odata.core.edm.EdmSingle;
import com.sap.core.odata.core.edm.EdmString;
import com.sap.core.odata.core.edm.EdmTime;
import com.sap.core.odata.core.edm.Uint7;
import com.sap.core.odata.core.uri.expression.FilterParserImpl;

public class ParserTest {

  static class ParserTool
  {
    private String expression;
    private CommonExpression tree;
    private CommonExpression curNode;

    public ParserTool(String expression, FilterExpression root) {
      System.out.println("ParserTool - Testing: " + expression);
      this.expression = expression;
      this.tree = root.getExpression();
      this.curNode = this.tree;
    }

    ParserTool aKind(ExpressionKind kind)
    {
      String info = "GetInfoKind(" + expression + ")-->";
      System.out.println("  " + info + "Expected: " + kind.toString() + " Actual: " + curNode.getKind().toString());

      assertEquals(info, curNode.getKind(), kind);
      return this;
    }

    public ParserTool aUriLiteral(String uriLiteral) {
      String info = "GetUriLiteral(" + expression + ")-->";
      System.out.println("  " + info + "Expected: " + uriLiteral + " Actual: " + curNode.toUriLiteral());

      assertEquals(info, curNode.toUriLiteral(), uriLiteral);
      return this;
    }

    public ParserTool aEdmType(EdmSimpleType boolInst) {
      String info = "GetEdmType(" + expression + ")-->";
      System.out.println("  " + info + "Expected: " + boolInst.toString() + " Actual: " + curNode.getEdmType().toString());

      assertEquals(info, curNode.getEdmType().equals(boolInst), true);
      return this;
    }

    public void aSerialized(String expected) {
      String actual;
      ExpressionVisitor visitor = new TestVisitor();
      actual = tree.accept(visitor).toString();

      String info = "GetSerialized(" + expression + ")-->";
      System.out.println("  " + info + "Expected: " + expected + " Actual: " + actual);

      assertEquals(info, expected, actual);
    }

  }

  static public ParserTool GetPTF(String expression)
  {
    try {
      FilterParserImpl parser = new FilterParserImpl(null, null);
      FilterExpression root = parser.ParseExpression(expression);
      return new ParserTool(expression, root);
    } catch (ExpressionException e) {
      fail("Error in parser" + e.getLocalizedMessage());
    }
    return null;
  }

  @Test
  public void TestSimpleUnaryOperator()
  {
    GetPTF("not true").aSerialized("{NOT {true}}");
    GetPTF("- 2d").aSerialized("{MINUS {2d}}");
  }

  @Test
  public void TestSinglePlainLiterals()
  {
    //assertEquals("Hier", 44, 33);
    //---
    //Checks from EdmSimpleType test
    //---
    EdmBoolean boolInst = EdmBoolean.getInstance();
    EdmBinary binaryInst = EdmBinary.getInstance();
    Bit bitInst = Bit.getInstance();
    EdmByte byteInst = EdmByte.getInstance();
    Uint7 Uint7Inst = Uint7.getInstance();
    EdmDateTime datetimeInst = EdmDateTime.getInstance();
    EdmDateTimeOffset datetimeOffsetInst = EdmDateTimeOffset.getInstance();
    EdmDecimal decimalInst = EdmDecimal.getInstance();
    EdmDouble doubleInst = EdmDouble.getInstance();
    EdmGuid guidInst = EdmGuid.getInstance();
    EdmInt16 int16Inst = EdmInt16.getInstance();
    EdmInt32 int32Inst = EdmInt32.getInstance();
    EdmInt64 int64Inst = EdmInt64.getInstance();
    EdmSByte intSByte = EdmSByte.getInstance();
    EdmSingle singleInst = EdmSingle.getInstance();
    EdmString stringInst = EdmString.getInstance();
    EdmTime timeInst = EdmTime.getInstance();

    GetPTF("X'Fa12aAA1'").aUriLiteral("X'Fa12aAA1'").aKind(ExpressionKind.LITERAL).aEdmType(binaryInst);
    GetPTF("binary'FA12AAA1'").aUriLiteral("binary'FA12AAA1'").aKind(ExpressionKind.LITERAL).aEdmType(binaryInst);

    GetPTF("true").aUriLiteral("true").aKind(ExpressionKind.LITERAL).aEdmType(boolInst);
    GetPTF("false").aUriLiteral("false").aKind(ExpressionKind.LITERAL).aEdmType(boolInst);

    GetPTF("1").aUriLiteral("1").aKind(ExpressionKind.LITERAL).aEdmType(bitInst);
    GetPTF("0").aUriLiteral("0").aKind(ExpressionKind.LITERAL).aEdmType(bitInst);

    GetPTF("255").aUriLiteral("255").aKind(ExpressionKind.LITERAL).aEdmType(byteInst);

    GetPTF("123").aUriLiteral("123").aKind(ExpressionKind.LITERAL).aEdmType(Uint7Inst);

    GetPTF("datetime'2009-12-26T21%3A23%3A38'").aUriLiteral("datetime'2009-12-26T21%3A23%3A38'").aKind(ExpressionKind.LITERAL).aEdmType(datetimeInst);
    GetPTF("datetime'2009-12-26T21%3A23%3A38'").aUriLiteral("datetime'2009-12-26T21%3A23%3A38'").aKind(ExpressionKind.LITERAL).aEdmType(datetimeInst);

    GetPTF("datetimeoffset'2009-12-26T21%3A23%3A38Z'").aUriLiteral("datetimeoffset'2009-12-26T21%3A23%3A38Z'").aKind(ExpressionKind.LITERAL).aEdmType(datetimeOffsetInst);
    GetPTF("datetimeoffset'2002-10-10T12%3A00%3A00-05%3A00'").aUriLiteral("datetimeoffset'2002-10-10T12%3A00%3A00-05%3A00'").aKind(ExpressionKind.LITERAL).aEdmType(datetimeOffsetInst);

    GetPTF("4.5m").aUriLiteral("4.5m").aKind(ExpressionKind.LITERAL).aEdmType(decimalInst);
    GetPTF("4.5M").aUriLiteral("4.5M").aKind(ExpressionKind.LITERAL).aEdmType(decimalInst);

    GetPTF("4.5d").aUriLiteral("4.5d").aKind(ExpressionKind.LITERAL).aEdmType(doubleInst);
    GetPTF("4.5D").aUriLiteral("4.5D").aKind(ExpressionKind.LITERAL).aEdmType(doubleInst);

    GetPTF("guid'1225c695-cfb8-4ebb-aaaa-80da344efa6a'").aUriLiteral("guid'1225c695-cfb8-4ebb-aaaa-80da344efa6a'").aKind(ExpressionKind.LITERAL).aEdmType(guidInst);

    GetPTF("-32768").aUriLiteral("-32768").aKind(ExpressionKind.LITERAL).aEdmType(int16Inst);
    GetPTF("3276").aUriLiteral("3276").aKind(ExpressionKind.LITERAL).aEdmType(int16Inst);
    GetPTF("32767").aUriLiteral("32767").aKind(ExpressionKind.LITERAL).aEdmType(int16Inst);

    GetPTF("-327687").aUriLiteral("-327687").aKind(ExpressionKind.LITERAL).aEdmType(int32Inst);
    GetPTF("32768").aUriLiteral("32768").aKind(ExpressionKind.LITERAL).aEdmType(int32Inst);
    GetPTF("327686").aUriLiteral("327686").aKind(ExpressionKind.LITERAL).aEdmType(int32Inst);

    GetPTF("64L").aUriLiteral("64L").aKind(ExpressionKind.LITERAL).aEdmType(int64Inst);
    GetPTF("64l").aUriLiteral("64l").aKind(ExpressionKind.LITERAL).aEdmType(int64Inst);

    GetPTF("-123").aUriLiteral("-123").aKind(ExpressionKind.LITERAL).aEdmType(intSByte);
    GetPTF("-128").aUriLiteral("-128").aKind(ExpressionKind.LITERAL).aEdmType(intSByte);

    GetPTF("4.5f").aUriLiteral("4.5f").aKind(ExpressionKind.LITERAL).aEdmType(singleInst);

    GetPTF("'abc'").aUriLiteral("'abc'").aKind(ExpressionKind.LITERAL).aEdmType(stringInst);
    GetPTF("time'P120D'").aUriLiteral("time'P120D'").aKind(ExpressionKind.LITERAL).aEdmType(timeInst);

    //The EdmSimpleTypeSamples contains a well formatted list of all possible 
    //UriLiterals for SimpleTypes, instances for their Type classes and their Values in java notation
    /*
    for ( EdmSimpleTypeSamples.UriTypeValueSet [] utvSetSet : EdmSimpleTypeSamples.someAll )
    {
      for( EdmSimpleTypeSamples.UriTypeValueSet utvSet : utvSetSet)
      {
        GetPTF(utvSet.uri).aKind(ExpressionKind.LITERAL).aEdmType(utvSet.type).aUriLiteral(utvSet.uri);
      }
    }*/

  }

  public void TestSinglePlainLiteralsABAP()
  {
    //---
    //Checks from ABAP
    //---
    //GetPTF("X'1234567890ABCDEF'").aKind(ExpressionKind.LITERAL);

    /*    
        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>binary( ).
        lcl_helper=>veri_type( iv_expression = `X'1234567890ABCDEF'` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>boolean( ).
        lcl_helper=>veri_type( iv_expression = `true` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>get_instance( iv_name = 'Bit' ).
        lcl_helper=>veri_type( iv_expression = `1` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>get_instance( iv_name = 'Bit' ).
        lcl_helper=>veri_type( iv_expression = `0` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>get_instance( iv_name = 'UInt7' ).
        lcl_helper=>veri_type( iv_expression = `123` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>byte( ).
        lcl_helper=>veri_type( iv_expression = `130` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>datetime( ).
        lcl_helper=>veri_type( iv_expression = `datetime'2011-07-31T23:30:59'` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>datetimeoffset( ).
        lcl_helper=>veri_type( iv_expression = `datetimeoffset'2002-10-10T12:00:00-05:00'` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>decimal( ).
        lcl_helper=>veri_type( iv_expression = `1.1M` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>double( ).
        lcl_helper=>veri_type( iv_expression = `1.1D` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>double( ).
        lcl_helper=>veri_type( iv_expression = `1.1E+02D` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>guid( ).
        lcl_helper=>veri_type( iv_expression = `guid'12345678-1234-1234-1234-123456789012'` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>float( ).
        lcl_helper=>veri_type( iv_expression = `1.1F` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int16( ).
        lcl_helper=>veri_type( iv_expression = `12345` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int32( ).
        lcl_helper=>veri_type( iv_expression = `1234512345` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>int64( ).
        lcl_helper=>veri_type( iv_expression = `12345L` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>sbyte( ).
        lcl_helper=>veri_type( iv_expression = `-12` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>single( ).
        lcl_helper=>veri_type( iv_expression = `1.1F` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>string( ).
        lcl_helper=>veri_type( iv_expression = `'TEST'` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>time( ).
        lcl_helper=>veri_type( iv_expression = `time'P1998Y02M01D'` io_expected_type = lo_simple_type ).

        lo_simple_type = /iwcor/cl_ds_edm_simple_type=>time( ).
        lcl_helper=>veri_type( iv_expression = `time'P1998Y02M01DT23H14M33S'` io_expected_type = lo_simple_type ).
    */

  }
}
