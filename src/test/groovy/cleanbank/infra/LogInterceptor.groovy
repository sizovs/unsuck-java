package cleanbank.infra

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IAnnotationDrivenExtension
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.SpecInfo

import static org.slf4j.Logger.ROOT_LOGGER_NAME

class LogInterceptor extends AbstractMethodInterceptor implements IAnnotationDrivenExtension<EnableLogInterception> {

  @Override
  void visitSpecAnnotation(EnableLogInterception annotation, SpecInfo spec) {
    def interceptor = new Interceptor(spec)
    spec.addSetupInterceptor(interceptor)
    spec.addCleanupInterceptor(interceptor)
  }

  static class Interceptor extends AbstractMethodInterceptor {
    private final SpecInfo spec

    Interceptor(SpecInfo spec) {
      this.spec = spec
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) {
      def logField = spec.allFields.find { it -> it.type === Logs }
      if (logField) {
        def logger = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME)
        def logs = new Logs(logger)
        logs.listen()
        logField.writeValue(invocation.instance, logs)
      }

      invocation.proceed()
    }

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) {
      def logField = spec.allFields.find { it -> it.type === Logs }
      if (logField) {
        def logs = (Logs) logField.readValue(invocation.instance)
        logs.forget()
      }
      invocation.proceed()
    }
  }
}
