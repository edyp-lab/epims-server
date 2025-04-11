package fr.edyp.epims.database.entities;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "analysis_price_list", schema = "public")
public class AnalysisPriceList {

    private int id;
    private Date date;

    private Set<AnalysisPriceList2Item> analysisPriceList2Items = new HashSet<AnalysisPriceList2Item>(0);

    public AnalysisPriceList() {
    }


    public AnalysisPriceList(Date date, Set<AnalysisPriceList2Item> analysisPriceList2Items) {
        this.id = id;
        this.date = date;
        this.analysisPriceList2Items = analysisPriceList2Items;
    }

    public AnalysisPriceList(int id, Date date, Set<AnalysisPriceList2Item> analysisPriceList2Items) {
        this.id = id;
        this.date = date;
        this.analysisPriceList2Items = analysisPriceList2Items;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "analysis_price_list_generator")
    @SequenceGenerator(name = "analysis_price_list_generator", sequenceName = "analysis_price_list_generator_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Column(name = "date")
    public Date getDate() {
        return this.date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "analysisPriceList")
    public Set<AnalysisPriceList2Item> getAnalysisPriceList2Items() {
        return this.analysisPriceList2Items;
    }

    public void setAnalysisPriceList2Items(Set<AnalysisPriceList2Item> analysisPriceList2Items) {
        this.analysisPriceList2Items = analysisPriceList2Items;
    }

}

