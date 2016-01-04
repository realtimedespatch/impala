package classes;

import interfaces.MessageService;

import org.springframework.beans.factory.FactoryBean;

public class NonStaticMessageServiceFactoryBean implements FactoryBean<Object> {

    public Object getObject() throws Exception {
        return new PrototypeMessageService();
    }

    public Class<?> getObjectType() {
        return MessageService.class;
    }

    public boolean isSingleton() {
        return false;
    }

}
