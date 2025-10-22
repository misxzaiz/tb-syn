package org.example.syn.processor;

import org.example.tb.model.TbPageDTO;
import org.example.tb.model.TbPageReqDTO;

public interface DataProcessor<T> {
    TbPageDTO<T> process(TbPageReqDTO req);
}