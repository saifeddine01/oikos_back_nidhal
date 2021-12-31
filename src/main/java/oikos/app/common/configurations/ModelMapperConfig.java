package oikos.app.common.configurations;

import oikos.app.seller.Seller;
import oikos.app.seller.UpdateSellerRequest;
import oikos.app.serviceproviders.dtos.EditCompanyRequest;
import oikos.app.serviceproviders.models.ServiceCompany;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Created by Mohamed Haamdi on 08/02/2021 */
@Configuration
public class ModelMapperConfig {

  @Bean
  public ModelMapper modelMapper() {
    var modelMapper = new ModelMapper();
    modelMapper.getConfiguration().setSkipNullEnabled(true);
    modelMapper.addMappings(
        new PropertyMap<UpdateSellerRequest, Seller>() {
          @Override
          protected void configure() {
            skip(destination.getAddress());
          }
        });
    modelMapper.addMappings(
        new PropertyMap<EditCompanyRequest, ServiceCompany>() {
          @Override
          protected void configure() {
            skip(destination.getAddress());
          }
        });

    return modelMapper;
  }
}
