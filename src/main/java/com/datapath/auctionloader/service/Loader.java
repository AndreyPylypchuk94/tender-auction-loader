package com.datapath.auctionloader.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.data.domain.PageRequest.of;
import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
@Service
@RequiredArgsConstructor
public class Loader {

    @Value("${prozorro.auction.url}")
    private String url;
    @Value("${db.output.collection.name}")
    private String outputCollectionName;
    @Value("${db.input.collection.name}")
    private String inputCollectionName;

    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;

    @Scheduled(fixedDelay = 1000 * 60 * 60)
    public void load() {
        log.info("Loading started");

        String lastDate = getLastDate();

        Query query = new Query(
                where("type").is("lot").andOperator(where("status").is("complete"))
        );

        if (nonNull(lastDate))
            query.addCriteria(where("dateModified").gt(lastDate));

        int page = 0;
        List<Document> steps;
        do {
            steps = mongoTemplate.find(
                    query.with(of(page, 100, ASC, "dateModified")), Document.class, inputCollectionName
            );

            if (isEmpty(steps)) break;

            log.info("Extracted {} steps on {} page", steps.size(), page);

            steps.stream()
                    .map(this::load)
                    .filter(Objects::nonNull)
                    .forEach(this::persist);

            page++;
        } while (!isEmpty(steps));

        log.info("Loading finished");
    }

    private String getLastDate() {
        Query query = new Query().with(by(DESC, "stepDateModified"));
        Document document = mongoTemplate.findOne(query, Document.class, outputCollectionName);
        return isNull(document) || isNull(document.getString("stepDateModified")) ?
                null :
                document.getString("stepDateModified");
    }

    private Document load(Document step) {
        String tenderId = step.getString("tenderId");
        String lotId = step.getString("lotId");

        log.info("Loading {}_{}", tenderId, lotId);

        ResponseEntity<Document> response = restTemplate.getForEntity(url + String.join("_", tenderId, lotId), Document.class);

        if (200 != response.getStatusCodeValue())
            response = restTemplate.getForEntity(url + tenderId, Document.class);

        if (200 != response.getStatusCodeValue()) return null;

        Document auction = response.getBody();

        if (isNull(auction)) return null;

        auction.put("stepDateModified", step.getString("dateModified"));

        return auction;
    }

    private void persist(Document document) {
        mongoTemplate.save(document, outputCollectionName);
    }

}
