package effective.bank.application.infra.spring.collections;

import com.google.common.collect.ForwardingList;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
class SomeSpringBeans extends ForwardingList<SomeBean> {

    private final ObjectProvider<SomeBean> beanProvider;

    SomeSpringBeans(ObjectProvider<SomeBean> beanProvider) {
        this.beanProvider = beanProvider;
    }

    @Override
    protected List<SomeBean> delegate() {
        return beanProvider.stream().toList();
    }
}
