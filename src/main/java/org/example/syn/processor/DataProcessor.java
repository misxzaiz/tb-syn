package org.example.syn.processor;

import org.example.tb.model.BaseQueryRequest;
import org.example.tb.model.TbPageDTO;

public interface DataProcessor<T> {
    TbPageDTO<T> process(BaseQueryRequest req);
}