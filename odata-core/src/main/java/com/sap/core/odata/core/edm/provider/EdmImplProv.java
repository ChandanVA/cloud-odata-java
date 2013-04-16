package com.sap.core.odata.core.edm.provider;

import java.util.Collection;

import com.sap.core.odata.api.edm.Edm;
import com.sap.core.odata.api.edm.EdmAssociation;
import com.sap.core.odata.api.edm.EdmComplexType;
import com.sap.core.odata.api.edm.EdmEntityContainer;
import com.sap.core.odata.api.edm.EdmEntityType;
import com.sap.core.odata.api.edm.EdmException;
import com.sap.core.odata.api.edm.EdmServiceMetadata;
import com.sap.core.odata.api.edm.FullQualifiedName;
import com.sap.core.odata.api.edm.provider.EdmProvider;
import com.sap.core.odata.api.edm.provider.EntityType;
import com.sap.core.odata.api.edm.provider.Property;
import com.sap.core.odata.api.edm.provider.Schema;
import com.sap.core.odata.api.exception.ODataException;
import com.sap.core.odata.core.edm.EdmImpl;

public class EdmImplProv extends EdmImpl implements EdmServiceMetadata {

  protected EdmProvider edmProvider;
  double dataServiceVersion = 0;

  public EdmImplProv(EdmProvider edmProvider) {
    super();
    this.edmProvider = edmProvider;
    this.edmServiceMetadata = (EdmServiceMetadata) this;
  }

  @Override
  protected EdmEntityContainer createEntityContainer(String name) throws ODataException {
    return new EdmEntityContainerImplProv(this, edmProvider.getEntityContainer(name));
  }

  @Override
  protected EdmEntityType createEntityType(FullQualifiedName fqName) throws ODataException {
    return new EdmEntityTypeImplProv(this, edmProvider.getEntityType(fqName), fqName.getNamespace());
  }

  @Override
  protected EdmComplexType createComplexType(FullQualifiedName fqName) throws ODataException {
    return new EdmComplexTypeImplProv(this, edmProvider.getComplexType(fqName), fqName.getNamespace());
  }

  @Override
  protected EdmAssociation createAssociation(FullQualifiedName fqName) throws ODataException {
    return new EdmAssociationImplProv(this, edmProvider.getAssociation(fqName), fqName.getNamespace());
  }

  @Override
  public String getMetadata() throws EdmException {
    //Catch exception here to transform it into an edm exception?
    
    Collection<Schema> schemas;
    try {
      schemas = edmProvider.getSchemas();
    } catch (ODataException e) {
      throw new EdmException(EdmException.COMMON, e);
    }

    if (this.dataServiceVersion == 0) {

      this.dataServiceVersion = Edm.DATA_SERVICE_VERSION10;

      for (Schema schema : schemas) {
        for (EntityType entityType : schema.getEntityTypes()) {

          if (entityType.getCustomizableFeedMappings().getFcKeepInContent()) {
            dataServiceVersion = Edm.DATA_SERVICE_VERSION20;
            break;
          }

          for (Property property : entityType.getProperties()) {
            if (property.getCustomizableFeedMappings().getFcKeepInContent()) {
              dataServiceVersion = Edm.DATA_SERVICE_VERSION20;
              break;
            }
          }

          if (dataServiceVersion == Edm.DATA_SERVICE_VERSION20)
            break;
        }

        if (dataServiceVersion == Edm.DATA_SERVICE_VERSION20)
          break;
      }
    }
    //TODO: Convert Metadata into the right format
    return "" + dataServiceVersion + schemas;

    //    DATA:
    //      ls_metadata TYPE /IWCOR/if_DS_edm_provider=>data_services_s,
    //      lo_writer TYPE REF TO cl_sxml_string_writer,
    //      ld_schema TYPE REF TO /IWCOR/if_DS_edm_provider=>schema_s,
    //      ld_entity_type TYPE REF TO /IWCOR/if_DS_edm_provider=>entity_type_s,
    //      ld_property TYPE REF TO /IWCOR/if_DS_edm_provider=>property_s,
    //      lv_data_service_version type string,
    //      lo_error_message TYPE REF TO /IWCOR/cx_DS_error_message.
    //
    //    TRY.
    //        mo_provider->get_schemas( IMPORTING et_schema = ls_metadata-schemas ).
    //      CATCH /IWCOR/cx_DS_error_message INTO lo_error_message.
    //        RAISE EXCEPTION TYPE /IWCOR/cx_DS_edm_error
    //          EXPORTING
    //            previous = lo_error_message.
    //    ENDTRY.
    //
    //    lv_data_service_version = /IWCOR/if_DS_edm=>gc_data_service_version_10.
    //    LOOP AT ls_metadata-schemas REFERENCE INTO ld_schema.
    //      LOOP AT ld_schema->entity_types REFERENCE INTO ld_entity_type.
    //        LOOP AT ld_entity_type->properties REFERENCE INTO ld_property WHERE customizable_feed_mappings-fc_keep_in_content = /IWCOR/if_DS_edm=>gc_bool_false.
    //          lv_data_service_version = /IWCOR/if_DS_edm=>gc_data_service_version_20.
    //          EXIT.
    //        ENDLOOP.
    //        IF ld_entity_type->customizable_feed_mappings-fc_keep_in_content = /IWCOR/if_DS_edm=>gc_bool_false.
    //          lv_data_service_version = /IWCOR/if_DS_edm=>gc_data_service_version_20.
    //        ENDIF.
    //        IF lv_data_service_version = /IWCOR/if_DS_edm=>gc_data_service_version_20.
    //          EXIT.
    //        ENDIF.
    //      ENDLOOP.
    //      IF lv_data_service_version = /IWCOR/if_DS_edm=>gc_data_service_version_20.
    //        EXIT.
    //      ENDIF.
    //    ENDLOOP.
    //    ls_metadata-data_service_version = lv_data_service_version.
    //
    //    lo_writer = cl_sxml_string_writer=>create( ).
    //    /IWCOR/cl_DS_edm_metadata=>write_metadata( is_metadata = ls_metadata io_writer = lo_writer ).
    //    ev_metadata = lo_writer->get_output( ).
    //    ev_data_service_version = ls_metadata-data_service_version.
  }

  @Override
  public String getDataServiceVersion() throws EdmException{
    // cache it, when get metadate is called first, otherwise calculateDataServiceVersion in separate method,
    // the method is required in proc single to set header "DataServiceVersion" 
    if (this.dataServiceVersion == 0) {
      this.dataServiceVersion = calculateDataServiceVersion();
    }
    return "" + this.dataServiceVersion;
  }

  private double calculateDataServiceVersion() throws EdmException {
    double dataServiceVersion = Edm.DATA_SERVICE_VERSION10;
    
    Collection<Schema> schemas;
    try {
      schemas = edmProvider.getSchemas();
    } catch (ODataException e) {
     throw new EdmException(EdmException.COMMON,e);
    }
    
    for (Schema schema : schemas) {
      for (EntityType entityType : schema.getEntityTypes()) {

        if (entityType.getCustomizableFeedMappings().getFcKeepInContent()) {
          dataServiceVersion = Edm.DATA_SERVICE_VERSION20;
          break;
        }

        for (Property property : entityType.getProperties()) {
          if (property.getCustomizableFeedMappings().getFcKeepInContent()) {
            dataServiceVersion = Edm.DATA_SERVICE_VERSION20;
            break;
          }
        }

        if (dataServiceVersion == Edm.DATA_SERVICE_VERSION20)
          break;
      }

      if (dataServiceVersion == Edm.DATA_SERVICE_VERSION20)
        break;
    }
    return dataServiceVersion;
  }
}