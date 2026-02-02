package com.payv.ledger.application.port;

import java.util.Collection;
import java.util.Map;

public interface ClassificationQueryPort {
    Map<String, String> getTagNames(Collection<String> tagIds, String ownerUserId);
    Map<String, String> getCategoryNames(Collection<String> categoryIds, String ownerUserId);
}
