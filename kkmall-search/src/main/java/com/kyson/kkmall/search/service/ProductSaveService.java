package com.kyson.kkmall.search.service;

import com.kyson.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

public interface ProductSaveService {

    Boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;
}
