package org.springframework.samples.petclinic.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.NonTransientDataAccessResourceException;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.stereotype.Service;

@Service
@Qualifier("DemoService")
public class DemoServiceImpl implements DemoService {

	@Override
	public void savePetFailure(Pet pet) throws DataAccessException {
		throw new NonTransientDataAccessResourceException("Save pet failure as expectedly.");
	}

}
