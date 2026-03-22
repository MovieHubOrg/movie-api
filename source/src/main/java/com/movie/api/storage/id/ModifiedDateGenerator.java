package com.movie.api.storage.id;

import com.movie.api.storage.base.Auditable;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;
import java.util.Date;

public class ModifiedDateGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        try{
            Auditable reuseDate = (Auditable) o;
            if(reuseDate.getModifiedDate()!=null){
                return reuseDate.getModifiedDate();
            }
        }catch (Exception e){
            //e.printStackTrace();
        }
        return new Date();
    }
}
