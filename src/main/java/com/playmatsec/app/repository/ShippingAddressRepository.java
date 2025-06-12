package com.playmatsec.app.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.playmatsec.app.repository.model.ShippingAddress;
import com.playmatsec.app.repository.utils.SearchOperation;
import com.playmatsec.app.repository.utils.SearchStatement;
import com.playmatsec.app.repository.utils.SearchCriteria.ShippingAddressSearchCriteria;
import com.playmatsec.app.repository.utils.Consts.ShippingAddressConsts;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ShippingAddressRepository {
    private final ShippingAddressJpaRepository repository;

    public List<ShippingAddress> getShippingAddresses() {
        return repository.findAll();
    }

    public ShippingAddress getById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    public ShippingAddress save(ShippingAddress shippingAddress) {
        return repository.save(shippingAddress);
    }

    public void delete(ShippingAddress shippingAddress) {
        repository.delete(shippingAddress);
    }

    public List<ShippingAddress> search(String user, String fullname, String phone, String country, String state, String city, String postalCode, String addressOne, String addressTwo, Boolean current) {
        ShippingAddressSearchCriteria spec = new ShippingAddressSearchCriteria();
        if (StringUtils.isNotBlank(user)) {
            spec.add(new SearchStatement(ShippingAddressConsts.USER, user, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(fullname)) {
            spec.add(new SearchStatement(ShippingAddressConsts.FULLNAME, fullname, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(phone)) {
            spec.add(new SearchStatement(ShippingAddressConsts.PHONE, phone, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(country)) {
            spec.add(new SearchStatement(ShippingAddressConsts.COUNTRY, country, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(state)) {
            spec.add(new SearchStatement(ShippingAddressConsts.STATE, state, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(city)) {
            spec.add(new SearchStatement(ShippingAddressConsts.CITY, city, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(postalCode)) {
            spec.add(new SearchStatement(ShippingAddressConsts.POSTAL_CODE, postalCode, SearchOperation.EQUAL));
        }
        if (StringUtils.isNotBlank(addressOne)) {
            spec.add(new SearchStatement(ShippingAddressConsts.ADDRESS_ONE, addressOne, SearchOperation.MATCH));
        }
        if (StringUtils.isNotBlank(addressTwo)) {
            spec.add(new SearchStatement(ShippingAddressConsts.ADDRESS_TWO, addressTwo, SearchOperation.MATCH));
        }
        if (current != null) {
            spec.add(new SearchStatement(ShippingAddressConsts.CURRENT, current, SearchOperation.EQUAL));
        }

        return repository.findAll(spec);
    }
}
