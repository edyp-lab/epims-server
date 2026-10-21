package fr.edyp.epims.service;

import fr.edyp.epims.database.dao.AliquotageRepository;
import fr.edyp.epims.database.dao.ProtocolApplicationRepository;
import fr.edyp.epims.database.dao.SampleFamilyRepository;
import fr.edyp.epims.database.dao.SampleLocatorRepository;
import fr.edyp.epims.database.dao.SampleRepository;
import fr.edyp.epims.database.dao.SeparationRepository;
import fr.edyp.epims.database.dao.SeparationResultRepository;
import fr.edyp.epims.database.dao.SamplesSetRepository;
import fr.edyp.epims.database.dao.TreatmentsApplicationRepository;
import fr.edyp.epims.database.dao.TreatmentsRepository;
import fr.edyp.epims.database.entities.*;
import fr.edyp.epims.database.entitytojson.Converter;
import fr.edyp.epims.json.FragmentToCreateJson;
import fr.edyp.epims.json.FragmentsGroupToCreateJson;
import fr.edyp.epims.json.SampleJson;
import fr.edyp.epims.json.StudyJson;
import fr.edyp.epims.version.DatabaseVersionManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Service
public class FragmentCreationService {

    private final SampleRepository sampleRepository;
    private final TreatmentsRepository treatmentsRepository;
    private final TreatmentsApplicationRepository treatmentsApplicationRepository;
    private final SeparationResultRepository separationResultRepository;
    private final ProtocolApplicationRepository protocolApplicationRepository;
    private final SampleFamilyRepository sampleFamilyRepository;
    private final SampleLocatorRepository sampleLocatorRepository;
    private final SamplesSetRepository samplesSetRepository;
    private final AliquotageRepository aliquotageRepository;
    private final SeparationRepository separationRepository;

    public FragmentCreationService(SampleRepository sampleRepository, TreatmentsRepository treatmentsRepository, TreatmentsApplicationRepository treatmentsApplicationRepository, SeparationResultRepository separationResultRepository, ProtocolApplicationRepository protocolApplicationRepository, SampleFamilyRepository sampleFamilyRepository, SampleLocatorRepository sampleLocatorRepository, SamplesSetRepository samplesSetRepository, AliquotageRepository aliquotageRepository, SeparationRepository separationRepository) {
        this.sampleRepository = sampleRepository;
        this.treatmentsRepository = treatmentsRepository;
        this.treatmentsApplicationRepository = treatmentsApplicationRepository;
        this.separationResultRepository = separationResultRepository;
        this.protocolApplicationRepository = protocolApplicationRepository;
        this.sampleFamilyRepository = sampleFamilyRepository;
        this.sampleLocatorRepository = sampleLocatorRepository;
        this.samplesSetRepository = samplesSetRepository;
        this.aliquotageRepository = aliquotageRepository;
        this.separationRepository = separationRepository;
    }

    @Transactional
    public List<SampleJson> createFragments(FragmentsGroupToCreateJson fragmentsGroupToCreateJson) {
        Date today = new Date();
        String todayString = new SimpleDateFormat("yyyy-MM-dd").format(today);
        List<SampleJson> sampleJsons = new ArrayList<>();
        HashMap<String, ArrayList<FragmentToCreateJson>> fragmentsGroupsMap = fragmentsGroupToCreateJson.getParentToFragmentsMap();
        for (String parentSampleKey : fragmentsGroupsMap.keySet()) {
            ArrayList<FragmentToCreateJson> fragments = fragmentsGroupsMap.get(parentSampleKey);
            Sample parentSample = sampleRepository.findByName(parentSampleKey).get();
            Actor actor = parentSample.getActor();
            BiologicOrigin biologicOrigin = parentSample.getBiologicOrigin();
            Study study = parentSample.getStudy();
            Treatments treatments = parentSample.getTreatments();
            if (treatments == null) {
                treatments = treatmentsRepository.save(new Treatments(null, null, null, null));
                parentSample.setTreatments(treatments);
                parentSample = sampleRepository.save(parentSample);
            }
            Integer rank = 0;
            Set<TreatmentsApplication> treatmentsApplicationSet = treatments.getTreatmentsApplications();
            if (treatmentsApplicationSet != null) {
                rank = treatmentsApplicationSet.size();
            }
            String protocolName = "Frag-" + parentSample.getName() + "-" + todayString;
            SeparationResult separationResult = separationResultRepository.save(new SeparationResult("ResultOf_" + protocolName, null, null, null, null, null, null, null));
            ProtocolApplication protocolApplication = protocolApplicationRepository.save(new ProtocolApplication(actor, null, protocolName, today, null, null, null, null, null, null, null, null, null));
            TreatmentsApplicationId treatmentsApplicationId = new TreatmentsApplicationId(treatments.getId(), protocolApplication.getId());
            treatmentsApplicationRepository.save(new TreatmentsApplication(treatmentsApplicationId, protocolApplication, treatments, rank));
            for (FragmentToCreateJson fragment : fragments) {
                Sample sample = new Sample(fragment.getName(), actor, biologicOrigin, study, null, fragment.getDescription(), fragment.getVolume(), Sample.AVAILABLE_STATUS_VALUE, fragment.getQuantity(), parentSample.getOriginalName(), parentSample.getRadioactivity(), parentSample.getToxicity(), today, null, null, null, null, null, null, null);
                sample = sampleRepository.save(sample);
                sampleFamilyRepository.save(new SampleFamily(new SampleFamilyId(parentSample.getName(), sample.getName()), protocolApplication, sample, parentSample));
                sampleLocatorRepository.save(new SampleLocator(sample, separationResult));
                sampleJsons.add(Converter.convert(sample, null));
            }
            SamplesSet samplesSet = samplesSetRepository.save(new SamplesSet(separationResult, fragments.size()));
            separationResult.setSamplesSet(samplesSet);
            separationResultRepository.save(separationResult);
            Aliquotage aliquotage = new Aliquotage(null, fragments.size());
            Separation separation = new Separation(protocolApplication, separationResult, aliquotage, null, null, null, null);
            aliquotage.setSeparation(separation);
            protocolApplication.setSeparation(separation);
            aliquotageRepository.save(aliquotage);
            separationRepository.save(separation);
        }
        DatabaseVersionManager.getSingleton().bumpVersion(StudyJson.class, null);
        return sampleJsons;
    }
}