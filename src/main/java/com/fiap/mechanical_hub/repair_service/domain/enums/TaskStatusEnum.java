package com.fiap.mechanical_hub.repair_service.domain.enums;

public enum TaskStatusEnum {
    PENDENTE("Pendente", false),
    INICIADO("Iniciado", true),
    FINALIZADO("Finalizado", true);

    private final String displayName;
    private final boolean manuallySettable;

    TaskStatusEnum(String displayName, boolean manuallySettable) {
        this.displayName = displayName;
        this.manuallySettable = manuallySettable;
    }

    public String getDisplayName() { return displayName; }
    public boolean isManuallySettable() { return manuallySettable; }

    public static TaskStatusEnum fromString(String value) {
        for (TaskStatusEnum status : TaskStatusEnum.values()) {
            if (status.name().equals(value)) {
                return status;
            }
        }
        String validValues = java.util.Arrays.stream(TaskStatusEnum.values())
                .filter(TaskStatusEnum::isManuallySettable)
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(", "));
        throw new IllegalArgumentException("Status de tarefa inválido: '" + value + "'. Valores válidos: " + validValues);
    }
}
