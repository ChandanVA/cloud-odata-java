package com.sap.core.odata.ref.edm;

import java.util.HashMap;
import java.util.Map;

import com.sap.core.odata.api.edm.EdmConcurrencyMode;
import com.sap.core.odata.api.edm.EdmFacets;
import com.sap.core.odata.api.edm.EdmMultiplicity;
import com.sap.core.odata.api.edm.EdmTargetPath;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.Association;
import com.sap.core.odata.api.edm.provider.AssociationSet;
import com.sap.core.odata.api.edm.provider.ComplexType;
import com.sap.core.odata.api.edm.provider.CustomizableFeedMappings;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.edm.provider.EntityContainer;
import com.sap.core.odata.api.edm.provider.EntitySet;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.api.edm.provider.FunctionImport;
import com.sap.core.odata.api.edm.provider.FunctionImportParameter;
import com.sap.core.odata.api.edm.provider.Key;
import com.sap.core.odata.api.edm.provider.NavigationProperty;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.PropertyRef;
import com.sap.core.odata.api.edm.provider.ReturnType;
import com.sap.core.odata.api.exception.ODataMessageException;
import com.sap.core.odata.api.exception.ODataNotFoundException;
import com.sap.core.odata.api.exception.ODataRuntimeException;

public class ScenarioEdmProvider implements EdmProvider {

  private static final String NAMESPACE_EDM = "Edm";
  private static final String NAMESPACE_1 = "RefScenario";
  private static final String NAMESPACE_2 = "RefScenario2";

  private static final String ENTITY_TYPE_1_1 = "Employee";
  private static final String ENTITY_TYPE_1_BASE = "Base";
  private static final String ENTITY_TYPE_1_2 = "Team";
  private static final String ENTITY_TYPE_1_3 = "Room";
  private static final String ENTITY_TYPE_1_4 = "Manager";
  private static final String ENTITY_TYPE_1_5 = "Building";
  private static final String ENTITY_TYPE_2_1 = "Photo";

  private static final FullQualifiedName EDM_STRING = new FullQualifiedName("String", NAMESPACE_EDM);
  private static final FullQualifiedName EDM_INT16 = new FullQualifiedName("Int16", NAMESPACE_EDM);
  private static final FullQualifiedName EDM_DATETIME = new FullQualifiedName("DateTime", NAMESPACE_EDM);
  private static final FullQualifiedName EDM_BINARY = new FullQualifiedName("Binary", NAMESPACE_EDM);
  private static final FullQualifiedName EDM_BOOLEAN = new FullQualifiedName("Boolean", NAMESPACE_EDM);

  private static final String ROLE_1_1 = "r_Employees";
  private static final String ROLE_1_2 = "r_Team";
  private static final String ROLE_1_3 = "r_Room";
  private static final String ROLE_1_4 = "r_Manager";
  private static final String ROLE_1_5 = "r_Building";

  private static final String ENTITY_CONTAINER_1 = "Container1";
  private static final String ENTITY_CONTAINER_2 = "Container2";

  private static final String ENTITY_SET_1_1 = "Employees";
  private static final String ENTITY_SET_1_2 = "Teams";
  private static final String ENTITY_SET_1_3 = "Rooms";
  private static final String ENTITY_SET_1_4 = "Managers";
  private static final String ENTITY_SET_1_5 = "Buildings";
  private static final String ENTITY_SET_2_1 = "Photos";

  private static final String FUNCTION_IMPORT_1 = "EmployeeSearch";
  private static final String FUNCTION_IMPORT_2 = "AllLocations";
  private static final String FUNCTION_IMPORT_3 = "AllUsedRoomIds";
  private static final String FUNCTION_IMPORT_4 = "MaximalAge";
  private static final String FUNCTION_IMPORT_5 = "MostCommonLocation";
  private static final String FUNCTION_IMPORT_6 = "ManagerPhoto";
  private static final String FUNCTION_IMPORT_7 = "OldestEmployee";
  
  @Override
  public EntityType getEntityType(final FullQualifiedName edmFQName) throws ODataRuntimeException, ODataMessageException {
    if (NAMESPACE_1.equals(edmFQName.getNamespace()))
      if (ENTITY_TYPE_1_1.equals(edmFQName.getName())) {
        Map<String, Property> properties = new HashMap<String, Property>();
        properties.put("EmployeeId", new Property("EmployeeId", EDM_STRING, getFacets(false, null, null), null, null, null, null));
        properties.put("EmployeeName", new Property("EmployeeName", EDM_STRING, null, new CustomizableFeedMappings(null, null, null, null, null, EdmTargetPath.SyndicationTitle), null, null, null));
        properties.put("ManagerId", new Property("ManagerId", EDM_STRING, null, null, null, null, null));
        properties.put("ManagerId", new Property("ManagerId", EDM_STRING, null, null, null, null, null));
        properties.put("TeamId", new Property("TeamId", EDM_STRING, getFacets(null, 2, null), null, null, null, null));
        properties.put("RoomId", new Property("RoomId", EDM_STRING, null, null, null, null, null));
        properties.put("Location", new Property("Location", new FullQualifiedName("c_Location", NAMESPACE_1), null, null, null, null, null));
        properties.put("Age", new Property("Age", EDM_INT16, null, null, null, null, null));
        properties.put("EntryDate", new Property("EntryDate", EDM_DATETIME, getFacets(true, null, null), new CustomizableFeedMappings(null, null, null, null, null, EdmTargetPath.SyndicationUpdated), null, null, null));
        properties.put("ImageUrl", new Property("ImageUrl", EDM_STRING, null, null, null, null, null));
        Map<String, PropertyRef> keyProperty = new HashMap<String, PropertyRef>();
        keyProperty.put("EmployeeId", new PropertyRef("EmployeeId", null));
        Map<String, NavigationProperty> navigationProperties = new HashMap<String, NavigationProperty>();
        navigationProperties.put("ne_Manager", new NavigationProperty("ne_Manager", new FullQualifiedName("ManagerEmployees", NAMESPACE_1), ROLE_1_1, ROLE_1_4, null, null));
        navigationProperties.put("ne_Team", new NavigationProperty("ne_Team", new FullQualifiedName("TeamEmployees", NAMESPACE_1), ROLE_1_1, ROLE_1_2, null, null));
        navigationProperties.put("ne_Room", new NavigationProperty("ne_Room", new FullQualifiedName("RoomEmployees", NAMESPACE_1), ROLE_1_1, ROLE_1_3, null, null));
        return new EntityType(ENTITY_TYPE_1_1, null, false, properties, null, null, null, true, null, new Key(keyProperty, null), navigationProperties);

      } else if (ENTITY_TYPE_1_BASE.equals(edmFQName.getName())) {
        Map<String, Property> properties = new HashMap<String, Property>();
        properties.put("Id", new Property("Id", EDM_STRING, getFacets(false, null, "1"), null, null, null, null));
        properties.put("Name", new Property("Name", EDM_STRING, null, new CustomizableFeedMappings(null, null, null, null, null, EdmTargetPath.SyndicationTitle), null, null, null));
        Map<String, PropertyRef> keyProperty = new HashMap<String, PropertyRef>();
        keyProperty.put("Id", new PropertyRef("Id", null));
        return new EntityType(ENTITY_TYPE_1_BASE, null, true, properties, null, null, null, false, null, new Key(keyProperty, null), null);

      } else if (ENTITY_TYPE_1_2.equals(edmFQName.getName())) {
        Map<String, Property> properties = new HashMap<String, Property>();
        properties.put("isScrumTeam", new Property("isScrumTeam", EDM_BOOLEAN, getFacets(true, null, null), null, null, null, null));
        Map<String, NavigationProperty> navigationProperties = new HashMap<String, NavigationProperty>();
        navigationProperties.put("nt_Employees", new NavigationProperty("nt_Employees", new FullQualifiedName("TeamEmployees", NAMESPACE_1), ROLE_1_2, ROLE_1_1, null, null));
        return new EntityType(ENTITY_TYPE_1_2, new FullQualifiedName(ENTITY_TYPE_1_BASE, NAMESPACE_1), false, properties, null, null, null, false, null, null, navigationProperties);

      } else if (ENTITY_TYPE_1_3.equals(edmFQName.getName())) {
        Map<String, Property> properties = new HashMap<String, Property>();
        properties.put("Seats", new Property("Seats", EDM_INT16, null, null, null, null, null));
        properties.put("Version", new Property("Version", EDM_INT16, null, null, null, null, null));
        Map<String, NavigationProperty> navigationProperties = new HashMap<String, NavigationProperty>();
        navigationProperties.put("nr_Employees", new NavigationProperty("nr_Employees", new FullQualifiedName("RoomEmployees", NAMESPACE_1), ROLE_1_3, ROLE_1_1, null, null));
        navigationProperties.put("nr_Building", new NavigationProperty("nr_Building", new FullQualifiedName("BuildingRooms", NAMESPACE_1), ROLE_1_3, ROLE_1_5, null, null));
        return new EntityType(ENTITY_TYPE_1_3, new FullQualifiedName(ENTITY_TYPE_1_BASE, NAMESPACE_1), false, properties, null, null, null, false, null, null, navigationProperties);

      } else if (ENTITY_TYPE_1_4.equals(edmFQName.getName())) {
        Map<String, NavigationProperty> navigationProperties = new HashMap<String, NavigationProperty>();
        navigationProperties.put("nm_Employees", new NavigationProperty("nm_Employees", new FullQualifiedName("ManagerEmployees", NAMESPACE_1), ROLE_1_4, ROLE_1_1, null, null));
        return new EntityType(ENTITY_TYPE_1_4, new FullQualifiedName(ENTITY_TYPE_1_1, NAMESPACE_1), false, null, null, null, null, true, null, null, navigationProperties);

      } else if (ENTITY_TYPE_1_5.equals(edmFQName.getName())) {
        Map<String, Property> properties = new HashMap<String, Property>();
        properties.put("Id", new Property("Id", EDM_STRING, getFacets(false, null, null), null, null, null, null));
        properties.put("Name", new Property("Name", EDM_STRING, null, null, null, null, null));
        properties.put("Image", new Property("Image", EDM_BINARY, null, null, null, null, null));
        Map<String, PropertyRef> keyProperty = new HashMap<String, PropertyRef>();
        keyProperty.put("Id", new PropertyRef("Id", null));
        Map<String, NavigationProperty> navigationProperties = new HashMap<String, NavigationProperty>();
        navigationProperties.put("nb_Rooms", new NavigationProperty("nb_Rooms", new FullQualifiedName("BuildingRooms", NAMESPACE_1), ROLE_1_5, ROLE_1_3, null, null));
        return new EntityType(ENTITY_TYPE_1_5, null, false, properties, null, null, null, false, null, new Key(keyProperty, null), navigationProperties);

      } else {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }

    else
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public ComplexType getComplexType(final FullQualifiedName edmFQName) throws ODataRuntimeException, ODataMessageException {
    return null;
  }

  @Override
  public Association getAssociation(final FullQualifiedName edmFQName) throws ODataRuntimeException, ODataMessageException {
    return null;
  }

  @Override
  public EntityContainer getEntityContainer(final String name) throws ODataRuntimeException, ODataMessageException {
    if (name == null || ENTITY_CONTAINER_1.equals(name))
      return new EntityContainer(ENTITY_CONTAINER_1, null, true);
    else if (ENTITY_CONTAINER_2.equals(name))
      return new EntityContainer(name, null, false);
    else
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public EntitySet getEntitySet(final String entityContainer, final String name) throws ODataRuntimeException, ODataMessageException {
    if (ENTITY_CONTAINER_1.equals(entityContainer))
      if (ENTITY_SET_1_1.equals(name))
        return new EntitySet(name, new FullQualifiedName(ENTITY_TYPE_1_1, NAMESPACE_1), null, null);
      else if (ENTITY_SET_1_2.equals(name))
        return new EntitySet(name, new FullQualifiedName(ENTITY_TYPE_1_2, NAMESPACE_1), null, null);
      else if (ENTITY_SET_1_3.equals(name))
        return new EntitySet(name, new FullQualifiedName(ENTITY_TYPE_1_3, NAMESPACE_1), null, null);
      else if (ENTITY_SET_1_4.equals(name))
        return new EntitySet(name, new FullQualifiedName(ENTITY_TYPE_1_4, NAMESPACE_1), null, null);
      else if (ENTITY_SET_1_5.equals(name))
        return new EntitySet(name, new FullQualifiedName(ENTITY_TYPE_1_5, NAMESPACE_1), null, null);
      else
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    else if (ENTITY_CONTAINER_2.equals(entityContainer))
      if (ENTITY_SET_2_1.equals(name))
        return new EntitySet(name, new FullQualifiedName(ENTITY_TYPE_2_1, NAMESPACE_2), null, null);
      else
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);

    else
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public FunctionImport getFunctionImport(final String entityContainer, final String name) throws ODataRuntimeException, ODataMessageException {
    if (ENTITY_CONTAINER_1.equals(entityContainer))
      if (FUNCTION_IMPORT_1.equals(name)) {
        Map<String, FunctionImportParameter> parameters = new HashMap<String, FunctionImportParameter>();
        parameters.put("q", new FunctionImportParameter("q", null, EDM_STRING, getFacets(true, null, null), null, null, null));
        return new FunctionImport(name, new ReturnType(new FullQualifiedName("Employee", NAMESPACE_1), EdmMultiplicity.MANY), ENTITY_SET_1_1, "GET", parameters, null, null);
      } else if (FUNCTION_IMPORT_2.equals(name)) {
        return new FunctionImport(name, new ReturnType(new FullQualifiedName("c_Location", NAMESPACE_1), EdmMultiplicity.MANY), null, "GET", null, null, null);
      } else if (FUNCTION_IMPORT_3.equals(name)) {
        return new FunctionImport(name, new ReturnType(EDM_STRING, EdmMultiplicity.MANY), null, "GET", null, null, null);
      } else if (FUNCTION_IMPORT_4.equals(name)) {
        return new FunctionImport(name, new ReturnType(EDM_INT16, EdmMultiplicity.ONE), null, "GET", null, null, null);
      } else if (FUNCTION_IMPORT_5.equals(name)) {
        return new FunctionImport(name, new ReturnType(new FullQualifiedName("c_Location", NAMESPACE_1), EdmMultiplicity.ONE), null, "GET", null, null, null);
      } else if (FUNCTION_IMPORT_6.equals(name)) {
          Map<String, FunctionImportParameter> parameters = new HashMap<String, FunctionImportParameter>();
          parameters.put("Id", new FunctionImportParameter("Id", null, EDM_STRING, getFacets(false, null, null), null, null, null));
          return new FunctionImport(name, new ReturnType(EDM_BINARY, EdmMultiplicity.ONE), null, "GET", parameters, null, null);
      } else if (FUNCTION_IMPORT_7.equals(name)) {
        return new FunctionImport(name, new ReturnType(new FullQualifiedName("Employee", NAMESPACE_1), EdmMultiplicity.ZERO_TO_ONE), ENTITY_SET_1_1, "GET", null, null, null);
      } else {
        throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
      }
    else
      throw new ODataNotFoundException(ODataNotFoundException.ENTITY);
  }

  @Override
  public AssociationSet getAssociationSet(final String entityContainer, final FullQualifiedName association, final String sourceEntitySetName, final String sourceEntitySetRole) throws ODataRuntimeException, ODataMessageException {
    return null;
  }

  private EdmFacets getFacets(final Boolean nullable, final Integer maxLength, final String defaultValue) {
    return new EdmFacets() {

      @Override
      public Boolean isUnicode() {
        return null;
      }

      @Override
      public Boolean isNullable() {
        return nullable;
      }

      @Override
      public Boolean isFixedLength() {
        return null;
      }

      @Override
      public Integer getScale() {
        return null;
      }

      @Override
      public Integer getPrecision() {
        return null;
      }

      @Override
      public Integer getMaxLength() {
        return maxLength;
      }

      @Override
      public String getDefaultValue() {
        return defaultValue;
      }

      @Override
      public EdmConcurrencyMode getConcurrencyMode() {
        return null;
      }

      @Override
      public String getCollation() {
        return null;
      }
    };
  }

}
