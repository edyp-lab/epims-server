package fr.edyp.epims.database.entities;

import javax.persistence.*;

@Entity
@Table(name = "analysis_price_list_2_item", schema = "public")
public class AnalysisPriceList2Item implements java.io.Serializable {


    private int id;
    private AnalysisPriceList analysisPriceList;
    private AnalysisPriceItem analysisPriceItem;


    public AnalysisPriceList2Item() {
    }

    public AnalysisPriceList2Item(AnalysisPriceList analysisPriceList, AnalysisPriceItem analysisPriceItem) {
        this.analysisPriceList = analysisPriceList;
        this.analysisPriceItem = analysisPriceItem;
    }

    public AnalysisPriceList2Item(int id, AnalysisPriceList analysisPriceList, AnalysisPriceItem analysisPriceItem) {
        this.id = id;
        this.analysisPriceList = analysisPriceList;
        this.analysisPriceItem = analysisPriceItem;
    }

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE, generator = "analysis_price_list_2_item_generator")
    @SequenceGenerator(name = "analysis_price_list_2_item_generator", sequenceName = "analysis_price_list_2_item_id_seq", allocationSize = 1)
    @Column(name = "id", unique = true, nullable = false)
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_price_list", nullable = false)
    public AnalysisPriceList getAnalysisPriceList() {
        return this.analysisPriceList;
    }

    public void setAnalysisPriceList(AnalysisPriceList analysisPriceList) {
        this.analysisPriceList = analysisPriceList;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_price_item", nullable = false)
    public AnalysisPriceItem getAnalysisPriceItem() {
        return this.analysisPriceItem;
    }

    public void setAnalysisPriceItem(AnalysisPriceItem analysisPriceItem) {
        this.analysisPriceItem = analysisPriceItem;
    }

}
