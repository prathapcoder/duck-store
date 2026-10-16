package com.duckstore.service.event;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * The Subject of the Observer pattern. It knows only the {@link WarehouseListener} interface.
 * Inside a transaction the notification waits for the commit, so listeners never hear about a change that was
 * rolled back. A failing listener is logged and never breaks the use case or the other listeners.
 */
@Component
public class WarehouseEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(WarehouseEventPublisher.class);

    private final List<WarehouseListener> listeners;

    public WarehouseEventPublisher(List<WarehouseListener> listeners) {
        this.listeners = List.copyOf(listeners);
    }

    public void publish(WarehouseEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    notifyListeners(event);
                }
            });
        } else {
            notifyListeners(event);
        }
    }

    private void notifyListeners(WarehouseEvent event) {
        for (WarehouseListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (RuntimeException e) {
                log.warn("Listener {} failed for {}", listener.getClass().getSimpleName(), event, e);
            }
        }
    }
}
