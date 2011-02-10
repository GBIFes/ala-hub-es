/**************************************************************************
 *  Copyright (C) 2010 Atlas of Living Australia
 *  All Rights Reserved.
 *
 *  The contents of this file are subject to the Mozilla Public
 *  License Version 1.1 (the "License"); you may not use this file
 *  except in compliance with the License. You may obtain a copy of
 *  the License at http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an "AS
 *  IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  rights and limitations under the License.
 ***************************************************************************/

package org.ala.hubs.controller;

import java.util.HashMap;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import org.ala.biocache.dto.SearchResultDTO;
import org.ala.biocache.dto.SearchRequestParams;
import org.ala.hubs.service.BiocacheService;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Search controller for occurrence record searching
 *
 * @author Nick dos Remedios (Nick.dosRemedios@csiro.au)
 */
@Controller("searchController")
public class SearchController {

	private final static Logger logger = Logger.getLogger(SearchController.class);
	
	/** Name of views */
	private final String SEARCH_LIST = "occurrences/list"; 
    @Inject
    private BiocacheService biocacheService;    
    
	
	/**
     * Performs a search for occurrence records via Biocache web services
     * 
     * @param requestParams
     * @param result
     * @param model
     * @param request
     * @return view
     * @throws Exception 
     */
	@RequestMapping(value = "/search*", method = RequestMethod.GET)
	public String search(SearchRequestParams requestParams, Model model,
            HttpServletRequest request) throws Exception {
		
		if (StringUtils.isEmpty(requestParams.getQ())) {
			return SEARCH_LIST;
		} else if (request.getParameter("pageSize") == null) {
            requestParams.setPageSize(20);
        }
		
        //reverse the sort direction for the "score" field a normal sort should be descending while a reverse sort should be ascending
        //sortDirection = getSortDirection(sortField, sortDirection);
        
		String view = SEARCH_LIST;
        SearchResultDTO searchResult = biocacheService.findByFulltextQuery(requestParams);
        logger.debug("searchResult: " + searchResult.getTotalRecords());
        model.addAttribute("searchResults", searchResult);
        model.addAttribute("facetMap", addFacetMap(requestParams.getFq()));
        model.addAttribute("lastPage", calculateLastPage(searchResult.getTotalRecords(), requestParams.getPageSize()));
        logger.debug("Selected view: "+view);
        
		return view;
	}
    
    /**
     * Create a HashMap for the filter queries
     *
     * @param filterQuery
     * @return
     */
    private HashMap<String, String> addFacetMap(String[] filterQuery) {
               HashMap<String, String> facetMap = new HashMap<String, String>();

        if (filterQuery != null && filterQuery.length > 0) {
            logger.debug("filterQuery = "+StringUtils.join(filterQuery, "|"));
            for (String fq : filterQuery) {
                if (fq != null && !fq.isEmpty()) {
                    String[] fqBits = StringUtils.split(fq, ":", 2);
                    facetMap.put(fqBits[0], fqBits[1]);
                }
            }
        }
        return facetMap;
    }
    
    /**
     * Calculate the last page number for pagination
     * 
     * @param totalRecords
     * @param pageSize
     * @return
     */
    private Integer calculateLastPage(Long totalRecords, Integer pageSize) {
        Integer lastPage = 0;
        Integer lastRecordNum = totalRecords.intValue();
        
        if (pageSize > 0) {
            lastPage = (lastRecordNum / pageSize) + ((lastRecordNum % pageSize > 0) ? 1 : 0);
        }
        
        return lastPage;
    }
}
