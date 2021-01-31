package com.github.shaart.jenkins2discord.notification.auto;

import com.github.shaart.jenkins2discord.notification.exception.StartupException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class OnStartValidator implements ApplicationListener<ContextRefreshedEvent> {

  private final List<AutoValidator> autoValidators;

  @Override
  public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
    autoValidate();
  }

  public void autoValidate() {
    List<Exception> errors = new ArrayList<>();
    autoValidators.forEach(runValidation(errors));

    if (!errors.isEmpty()) {
      log.error("Got startup errors:");
      StartupException startupException = new StartupException(
          "There are startup errors (count = " + errors.size() + "). Please check the log above.");
      for (int i = 0; i < errors.size(); i++) {
        Exception exception = errors.get(i);
        startupException.addSuppressed(exception);
        log.error("Startup error #" + (i + 1) + ": " + exception.getMessage(), exception);
      }
      throw startupException;
    }
  }

  private Consumer<AutoValidator> runValidation(@NonNull List<Exception> errors) {
    return validator -> {
      String validatorName = validator.getClass().getSimpleName();
      log.info("Running validator: {}", validatorName);
      try {
        validator.validate();
        log.info("{}: SUCCESS", validatorName);
      } catch (Exception e) {
        log.error("{}: FAIL", validatorName);
        errors.add(e);
      }
    };
  }
}
