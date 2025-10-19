package org.example.syn.processor;

import org.example.tb.model.TbPageReqDTO;
import org.example.tb.model.TbTotalPageDTO;

public interface DataProcessor<T> {
    TbTotalPageDTO<T> process(TbPageReqDTO req);
}