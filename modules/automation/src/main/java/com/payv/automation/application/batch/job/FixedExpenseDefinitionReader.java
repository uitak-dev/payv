package com.payv.automation.application.batch.job;

import com.payv.automation.domain.model.FixedExpenseDefinition;
import org.springframework.batch.item.ItemReader;

import java.util.Iterator;
import java.util.List;

/**
 * 단순 List 기반 ItemReader.
 *
 * DB Cursor/Paging Reader 대신, 이미 조회된 due definition 목록을 순차 반환한다.
 */
public class FixedExpenseDefinitionReader implements ItemReader<FixedExpenseDefinition> {

    private final Iterator<FixedExpenseDefinition> iterator;

    public FixedExpenseDefinitionReader(List<FixedExpenseDefinition> dueDefinitions) {
        this.iterator = dueDefinitions.iterator();
    }

    @Override
    public FixedExpenseDefinition read() {
        if (!iterator.hasNext()) {
            // Spring Batch 규약: null 반환 시 입력 종료
            return null;
        }
        return iterator.next();
    }
}
