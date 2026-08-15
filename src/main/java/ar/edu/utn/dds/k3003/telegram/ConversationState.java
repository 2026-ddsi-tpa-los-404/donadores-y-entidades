package ar.edu.utn.dds.k3003.telegram;

import java.util.Map;

public record ConversationState(String step, Map<String, String> data) {}
