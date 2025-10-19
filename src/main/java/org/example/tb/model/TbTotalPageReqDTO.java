package org.example.tb.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TbTotalPageReqDTO<T> {
    private TbPageReqDTO pageReq;
    private Function<TbPageReqDTO, TbPageDTO<T>> pageReqFunc;
    
}
