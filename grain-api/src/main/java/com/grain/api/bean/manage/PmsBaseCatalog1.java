package com.grain.api.bean.manage;


import lombok.Data;

import javax.persistence.*;
import java.beans.Transient;
import java.io.Serializable;
import java.util.List;

/**
 * @param
 * @return
 */
@Data
public class PmsBaseCatalog1 implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String name;


    private List<PmsBaseCatalog2> catalog2s;


}

