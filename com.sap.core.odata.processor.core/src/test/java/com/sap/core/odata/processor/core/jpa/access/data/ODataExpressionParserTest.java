package com.sap.core.odata.processor.core.jpa.access.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;

import org.easymock.EasyMock;
import org.junit.Test;

import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmLiteralKind;
import com.sap.core.odata.api.edm.EdmMapping;
import com.sap.core.odata.api.edm.EdmProperty;
import com.sap.core.odata.api.edm.EdmSimpleType;
import com.sap.core.odata.api.edm.EdmSimpleTypeKind;
import com.sap.core.odata.api.edm.EdmTypeKind;
import com.sap.core.odata.api.edm.EdmTyped;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.api.uri.KeyPredicate;
import com.sap.core.odata.api.uri.expression.BinaryExpression;
import com.sap.core.odata.api.uri.expression.BinaryOperator;
import com.sap.core.odata.api.uri.expression.CommonExpression;
import com.sap.core.odata.api.uri.expression.ExpressionKind;
import com.sap.core.odata.api.uri.expression.FilterExpression;
import com.sap.core.odata.api.uri.expression.LiteralExpression;
import com.sap.core.odata.api.uri.expression.MemberExpression;
import com.sap.core.odata.api.uri.expression.PropertyExpression;
import com.sap.core.odata.api.uri.expression.UnaryExpression;
import com.sap.core.odata.api.uri.expression.UnaryOperator;
import com.sap.core.odata.processor.api.jpa.exception.ODataJPARuntimeException;
import com.sap.core.odata.processor.api.jpa.jpql.JPQLStatement.Operator;

public class ODataExpressionParserTest {
	private static final String TABLE_ALIAS = "gwt1"; //$NON-NLS-1$

	@Test
	public void testParseWhereExpression() throws ODataException {

		String parsedStr = "";

		// Simple Binary query -
		parsedStr = ODataExpressionParser.parseToJPAWhereExpression(
				getBinaryExpressionMockedObj(BinaryOperator.EQ,
						ExpressionKind.PROPERTY, "SalesOrder", "1234"),
				TABLE_ALIAS);
		assertEquals(getAliasedProperty("SalesOrder") + " = 1234", parsedStr);

		// complex query -
		parsedStr = "";

		CommonExpression exp1 = getBinaryExpressionMockedObj(BinaryOperator.GE,
				ExpressionKind.PROPERTY, "SalesOrder", "1234");
		CommonExpression exp2 = getBinaryExpressionMockedObj(BinaryOperator.NE,
				ExpressionKind.PROPERTY, "SalesABC", "XYZ");

		parsedStr = parsedStr.concat(ODataExpressionParser
				.parseToJPAWhereExpression(
						getBinaryExpression(exp1, BinaryOperator.AND, exp2),
						TABLE_ALIAS));
		assertEquals(getAliasedProperty("SalesOrder") + " >= 1234 AND "
				+ getAliasedProperty("SalesABC") + " <> XYZ", parsedStr);
	}

	@Test
	public void testMoreThanOneBinaryExpression() throws ODataException {
		// complex query -
		String parsedStr = "";
		CommonExpression exp1 = getBinaryExpressionMockedObj(BinaryOperator.GE,
				ExpressionKind.PROPERTY, "SalesOrder", "1234");
		CommonExpression exp2 = getBinaryExpressionMockedObj(BinaryOperator.NE,
				ExpressionKind.PROPERTY, "SalesABC", "XYZ");

		parsedStr = ODataExpressionParser.parseToJPAWhereExpression(
				getBinaryExpression(exp1, BinaryOperator.AND, exp2),
				TABLE_ALIAS);
		assertEquals(getAliasedProperty("SalesOrder") + " >= 1234 AND "
				+ getAliasedProperty("SalesABC") + " <> XYZ", parsedStr);

		parsedStr = ODataExpressionParser
				.parseToJPAWhereExpression(
						getBinaryExpression(exp1, BinaryOperator.OR, exp2),
						TABLE_ALIAS);
		assertEquals(getAliasedProperty("SalesOrder") + " >= 1234 OR "
				+ getAliasedProperty("SalesABC") + " <> XYZ", parsedStr);
	}

	@Test
	public void testParseFilterExpression() throws ODataException {
		assertEquals(getAliasedProperty("SalesOrder"),
				ODataExpressionParser.parseToJPAWhereExpression(
						getFilterExpressionMockedObj(ExpressionKind.PROPERTY,
								"SalesOrder"), TABLE_ALIAS));
	}

	@Test
	public void testAllBinaryOperators() throws ODataException { // Test for all
																	// Binary
																	// Operators
		// complex query -
		String parsedStr1 = "";
		String parsedStr2 = "";

		CommonExpression exp1 = getBinaryExpressionMockedObj(BinaryOperator.LT,
				ExpressionKind.PROPERTY, "SalesOrder", "1234");
		CommonExpression exp2 = getBinaryExpressionMockedObj(BinaryOperator.LE,
				ExpressionKind.PROPERTY, "SalesABC", "XYZ");

		parsedStr1 = ODataExpressionParser.parseToJPAWhereExpression(
				(BinaryExpression) getBinaryExpression(exp1,
						BinaryOperator.AND, exp2), TABLE_ALIAS);
		assertEquals(getAliasedProperty("SalesOrder") + " < 1234 AND "
				+ getAliasedProperty("SalesABC") + " <= XYZ", parsedStr1);

		CommonExpression exp3 = getBinaryExpressionMockedObj(BinaryOperator.GT,
				ExpressionKind.PROPERTY, "LineItems", "2345");
		CommonExpression exp4 = getBinaryExpressionMockedObj(BinaryOperator.GE,
				ExpressionKind.PROPERTY, "SalesOrder", "Amazon");

		parsedStr2 = ODataExpressionParser.parseToJPAWhereExpression(
				getBinaryExpression(exp3, BinaryOperator.AND, exp4),
				TABLE_ALIAS);

		assertEquals(getAliasedProperty("LineItems") + " > 2345 AND "
				+ getAliasedProperty("SalesOrder") + " >= Amazon", parsedStr2);
	}

	@Test
	public void testParseMemberExpression() throws ODataException {
		assertEquals(
				getAliasedProperty("Address") + "." + "city" + " = "
						+ "\'City_3\'",
				ODataExpressionParser
						.parseToJPAWhereExpression(
								getBinaryExpression(
										getMemberExpressionMockedObj("Address",
												"city"),
										BinaryOperator.EQ,
										getLiteralExpressionMockedObj("\'City_3\'")),
								TABLE_ALIAS));
		assertEquals(
				getAliasedProperty("Address") + "." + "city" + "." + "area" + " = " + "\'BTM\'",
				ODataExpressionParser
					.parseToJPAWhereExpression(
							getBinaryExpression(
									getMultipleMemberExpressionMockedObj("Address","city","area"),
									BinaryOperator.EQ,
									getLiteralExpressionMockedObj("\'BTM\'")),
							TABLE_ALIAS));
	}
	
	private CommonExpression getMultipleMemberExpressionMockedObj(String string1,String string2, String string3) throws EdmException {
		
		MemberExpression memberExpression = EasyMock.createMock(MemberExpression.class);
		
		EasyMock.expect(memberExpression.getPath()).andStubReturn(getMemberExpressionMockedObj(string1, string2));
		EasyMock.expect(memberExpression.getProperty()).andStubReturn(getPropertyExpressionMockedObj(ExpressionKind.PROPERTY,string3));
		EasyMock.expect(memberExpression.getKind()).andStubReturn(ExpressionKind.MEMBER);
		EasyMock.replay(memberExpression);
		
		return memberExpression;
	}

	@Test
	public void testParseUnaryExpression() throws ODataException {

		UnaryExpression unaryExpression = getUnaryExpressionMockedObj(
				getPropertyExpressionMockedObj(ExpressionKind.PROPERTY,
						"deliveryStatus"),
				com.sap.core.odata.api.uri.expression.UnaryOperator.NOT);
		assertEquals(Operator.NOT + "(" + getAliasedProperty("deliveryStatus")
				+ ")", ODataExpressionParser.parseToJPAWhereExpression(
				unaryExpression, TABLE_ALIAS));

	}

	private UnaryExpression getUnaryExpressionMockedObj(
			CommonExpression operand, UnaryOperator unaryOperator) {
		UnaryExpression unaryExpression = EasyMock
				.createMock(UnaryExpression.class);
		EasyMock.expect(unaryExpression.getKind())
				.andStubReturn(ExpressionKind.UNARY);
		EasyMock.expect(unaryExpression.getOperand()).andStubReturn(operand)
				;
		EasyMock.expect(unaryExpression.getOperator()).andStubReturn(unaryOperator)
				;

		EasyMock.replay(unaryExpression);
		return unaryExpression;
	}

	private CommonExpression getMemberExpressionMockedObj(
			String pathUriLiteral, String propertyUriLiteral)
			throws EdmException {
		MemberExpression memberExpression = EasyMock
				.createMock(MemberExpression.class);
		EasyMock.expect(memberExpression.getPath())
				.andStubReturn(
						getPropertyExpressionMockedObj(ExpressionKind.PROPERTY,
								pathUriLiteral));
		EasyMock.expect(memberExpression.getProperty())
				.andStubReturn(
						getPropertyExpressionMockedObj(ExpressionKind.PROPERTY,
								propertyUriLiteral));
		EasyMock.expect(memberExpression.getKind())
				.andStubReturn(ExpressionKind.MEMBER);

		EasyMock.replay(memberExpression);
		return memberExpression;
	}

	private String getAliasedProperty(String property) {
		return TABLE_ALIAS + "." + property;
	}

	private LiteralExpression getLiteralExpressionMockedObj(String uriLiteral)
			throws EdmException {
		LiteralExpression rightOperandLiteralExpresion = EasyMock
				.createMock(LiteralExpression.class);
		EasyMock.expect(rightOperandLiteralExpresion.getKind())
				.andStubReturn(ExpressionKind.LITERAL);
		EasyMock.expect(rightOperandLiteralExpresion.getUriLiteral())
				.andStubReturn(uriLiteral);// "1234"
		EasyMock.expect(rightOperandLiteralExpresion.getEdmType())
				.andStubReturn(getEdmSimpleTypeMockedObj(uriLiteral));
		EasyMock.replay(rightOperandLiteralExpresion);
		return rightOperandLiteralExpresion;

	}

	private EdmSimpleType getEdmSimpleTypeMockedObj(String value)
			throws EdmException {
		EdmSimpleType edmSimpleType = EasyMock.createMock(EdmSimpleType.class);
		EasyMock.expect(edmSimpleType.getName()).andReturn(value);
		EasyMock.expect(edmSimpleType.getKind()).andStubReturn(EdmTypeKind.SIMPLE)
				;
		EasyMock.expect(
				edmSimpleType.valueOfString(value, EdmLiteralKind.URI,
						getEdmFacetsMockedObj(), null)).andStubReturn(value)
				;
		EasyMock.expect(
				edmSimpleType.valueOfString(value, EdmLiteralKind.URI, null,
						null)).andStubReturn(value);
		EasyMock.expect(
				edmSimpleType.valueToString(value, EdmLiteralKind.DEFAULT,
						getEdmFacetsMockedObj())).andStubReturn(value);
		EasyMock.expect(
				edmSimpleType
						.valueToString(value, EdmLiteralKind.DEFAULT, null))
				.andStubReturn(value);
		EasyMock.expect(edmSimpleType.getDefaultType()).andStubReturn(null)
				;
		EasyMock.replay(edmSimpleType);
		return edmSimpleType;
	}

	private EdmFacets getEdmFacetsMockedObj() {
		EdmFacets facets = EasyMock.createMock(EdmFacets.class);

		EasyMock.replay(facets);
		return facets;
	}

	private PropertyExpression getPropertyExpressionMockedObj(
			ExpressionKind expKind, String propertyName) throws EdmException {
		PropertyExpression leftOperandPropertyExpresion = EasyMock
				.createMock(PropertyExpression.class);
		EasyMock.expect(leftOperandPropertyExpresion.getKind())
				.andStubReturn(ExpressionKind.PROPERTY);
		EasyMock.expect(leftOperandPropertyExpresion.getPropertyName())
				.andStubReturn(propertyName);
		EasyMock.expect(leftOperandPropertyExpresion.getEdmProperty())
				.andStubReturn(getEdmTypedMockedObj(propertyName));
		EasyMock.replay(leftOperandPropertyExpresion);
		return leftOperandPropertyExpresion;
	}

	private EdmTyped getEdmTypedMockedObj(String propertyName)
			throws EdmException {
		EdmProperty mockedEdmProperty = EasyMock.createMock(EdmProperty.class);
		EasyMock.expect(mockedEdmProperty.getMapping())
				.andStubReturn(getEdmMappingMockedObj(propertyName));
		EasyMock.replay(mockedEdmProperty);
		return mockedEdmProperty;
	}

	private EdmMapping getEdmMappingMockedObj(String propertyName) {
		EdmMapping mockedEdmMapping = EasyMock.createMock(EdmMapping.class);
		EasyMock.expect(mockedEdmMapping.getInternalName())
				.andStubReturn(propertyName);
		EasyMock.replay(mockedEdmMapping);
		return mockedEdmMapping;
	}

	private BinaryExpression getBinaryExpressionMockedObj(
			BinaryOperator operator, ExpressionKind leftOperandExpKind,
			String propertyName, String literalStr) throws EdmException {
		BinaryExpression binaryExpression = EasyMock
				.createMock(BinaryExpression.class);
		EasyMock.expect(binaryExpression.getKind())
				.andStubReturn(ExpressionKind.BINARY);
		EasyMock.expect(binaryExpression.getLeftOperand())
				.andStubReturn(
						getPropertyExpressionMockedObj(leftOperandExpKind,
								propertyName));
		EasyMock.expect(binaryExpression.getOperator()).andStubReturn(operator)
				;
		EasyMock.expect(binaryExpression.getRightOperand())
				.andStubReturn(getLiteralExpressionMockedObj(literalStr));

		EasyMock.replay(binaryExpression);
		return binaryExpression;
	}

	private FilterExpression getFilterExpressionMockedObj(
			ExpressionKind leftOperandExpKind, String propertyName)
			throws EdmException {
		FilterExpression filterExpression = EasyMock
				.createMock(FilterExpression.class);
		EasyMock.expect(filterExpression.getKind())
				.andStubReturn(ExpressionKind.FILTER);
		EasyMock.expect(filterExpression.getExpression())
				.andStubReturn(
						getPropertyExpressionMockedObj(leftOperandExpKind,
								propertyName));

		EasyMock.replay(filterExpression);
		return filterExpression;
	}

	private CommonExpression getBinaryExpression(
			final CommonExpression leftOperand, final BinaryOperator operator,
			final CommonExpression rightOperand) {
		BinaryExpression binaryExpression = EasyMock
				.createMock(BinaryExpression.class);
		EasyMock.expect(binaryExpression.getKind())
				.andStubReturn(ExpressionKind.BINARY);
		EasyMock.expect(binaryExpression.getLeftOperand())
				.andStubReturn(leftOperand);
		EasyMock.expect(binaryExpression.getRightOperand())
				.andStubReturn(rightOperand);
		EasyMock.expect(binaryExpression.getOperator()).andStubReturn(operator)
				;

		EasyMock.replay(binaryExpression);
		return binaryExpression;
	}

	@Test
	public void testParseKeyPredicates() throws EdmException {
		// Setting up the expected value
		KeyPredicate keyPredicate1 = EasyMock.createMock(KeyPredicate.class);
		EdmProperty kpProperty1 = EasyMock.createMock(EdmProperty.class);
		EasyMock.expect(keyPredicate1.getLiteral()).andStubReturn("1");
		KeyPredicate keyPredicate2 = EasyMock.createMock(KeyPredicate.class);
		EdmProperty kpProperty2 = EasyMock.createMock(EdmProperty.class);
		EasyMock.expect(keyPredicate2.getLiteral()).andStubReturn("abc");
		EdmMapping edmMapping = EasyMock.createMock(EdmMapping.class);
		try {
			EasyMock.expect(kpProperty1.getName()).andStubReturn("field1");
			EasyMock.expect(kpProperty1.getType()).andStubReturn(
					EdmSimpleTypeKind.Int32.getEdmSimpleTypeInstance());
			EasyMock.expect(kpProperty2.getName()).andStubReturn("field2");
			EasyMock.expect(kpProperty2.getType()).andStubReturn(
					EdmSimpleTypeKind.String.getEdmSimpleTypeInstance());
		} catch (EdmException e2) {
			fail("this should not happen");
		}
		EasyMock.expect(keyPredicate1.getProperty()).andStubReturn(kpProperty1);
		EasyMock.expect(kpProperty1.getMapping()).andReturn(edmMapping);
		EasyMock.expect(edmMapping.getInternalName()).andReturn("field1");
		EasyMock.expect(keyPredicate2.getProperty()).andStubReturn(kpProperty2);
		EasyMock.expect(kpProperty2.getMapping()).andReturn(edmMapping);
		EasyMock.expect(edmMapping.getInternalName()).andReturn("field2");
		EasyMock.replay(edmMapping);
		EasyMock.replay(kpProperty1, keyPredicate1, kpProperty2, keyPredicate2);

		ArrayList<KeyPredicate> keyPredicates = new ArrayList<KeyPredicate>();
		keyPredicates.add(keyPredicate1);
		keyPredicates.add(keyPredicate2);
		String str = null;

		try {
			str = ODataExpressionParser.parseKeyPredicates(keyPredicates,
					TABLE_ALIAS);
		} catch (ODataJPARuntimeException e) {
			fail("this should not happen");
		}

		assertEquals("gwt1.field1 = 1 AND gwt1.field2 = 'abc'", str);
	}

	@Test
	public void testParseToJPASelectExpression() {

		ArrayList<String> selectedFields = new ArrayList<String>();
		selectedFields.add("BuyerAddress");
		selectedFields.add("BuyerName");
		selectedFields.add("BuyerId");

		assertEquals("gwt1.BuyerAddress, gwt1.BuyerName, gwt1.BuyerId",
				ODataExpressionParser.parseToJPASelectExpression(TABLE_ALIAS,
						selectedFields));
		assertEquals("gwt1", ODataExpressionParser.parseToJPASelectExpression(
				TABLE_ALIAS, null));

		selectedFields.clear();
		assertEquals("gwt1", ODataExpressionParser.parseToJPASelectExpression(
				TABLE_ALIAS, selectedFields));
	}
}
