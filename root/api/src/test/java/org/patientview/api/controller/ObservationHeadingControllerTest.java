package org.patientview.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.patientview.api.service.ObservationHeadingService;
import org.patientview.config.exception.ResourceNotFoundException;
import org.patientview.persistence.model.Group;
import org.patientview.persistence.model.ObservationHeading;
import org.patientview.persistence.model.ObservationHeadingGroup;
import org.patientview.persistence.model.enums.RoleName;
import org.patientview.persistence.repository.GroupRepository;
import org.patientview.persistence.repository.ObservationHeadingRepository;
import org.patientview.test.util.TestUtils;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Created by jamesr@solidstategroup.com
 * Created on 11/09/2014
 */
public class ObservationHeadingControllerTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private ObservationHeadingService observationHeadingService;

    @Mock
    private ObservationHeadingRepository observationHeadingRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private ObservationHeadingController observationHeadingController;

    private MockMvc mockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(observationHeadingController).build();
    }

    @Test
    public void testFindAll() {
        try {
            TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
            mockMvc.perform(MockMvcRequestBuilders
                    .get("/observationheading?page=0&size=5&sortDirection=ASC&sortField=name")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testGet() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        try {
            TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.SPECIALTY_ADMIN);
            mockMvc.perform(MockMvcRequestBuilders.get("/observationheading/" + observationHeading.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }

        try {
            verify(observationHeadingService, Mockito.times(1)).get(eq(observationHeading.getId()));
        } catch (ResourceNotFoundException e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testAdd() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        TestUtils.authenticateTestSingleGroupRole("testUser", "testGroup", RoleName.GLOBAL_ADMIN);

        try {
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);

            mockMvc.perform(MockMvcRequestBuilders.post("/observationheading")
                    .content(mapper.writeValueAsString(observationHeading))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).add(eq(observationHeading));
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testSave() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);

        try {
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);

            mockMvc.perform(MockMvcRequestBuilders.put("/observationheading")
                    .content(mapper.writeValueAsString(observationHeading))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).save(eq(observationHeading));
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
    }

    @Test
    public void testAddGroup() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        Group group = TestUtils.createGroup("GROUP1");
        group.setId(2L);

        try {
            when(observationHeadingRepository.findOne(eq(observationHeading.getId()))).thenReturn(observationHeading);
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);
            when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);

            mockMvc.perform(MockMvcRequestBuilders.post("/observationheading/" + observationHeading.getId() + "/group/"
                    + group.getId() + "/panel/3/panelorder/4"))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).addObservationHeadingGroup(eq(observationHeading.getId()),
                    eq(group.getId()), eq(3L), eq(4L));
        } catch (Exception e) {
            fail("Exception thrown" + e.getMessage());
        }
    }

    @Test
    public void testUpdateGroup() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        Group group = TestUtils.createGroup("GROUP1");
        group.setId(2L);

        try {
            when(observationHeadingRepository.findOne(eq(observationHeading.getId()))).thenReturn(observationHeading);
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);
            when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);

            mockMvc.perform(MockMvcRequestBuilders.post("/observationheading/" + observationHeading.getId() + "/group/"
                    + group.getId() + "/panel/3/panelorder/4"))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).addObservationHeadingGroup(eq(observationHeading.getId()),
                    eq(group.getId()), eq(3L), eq(4L));
        } catch (Exception e) {
            fail("Exception thrown" + e.getMessage());
        }
    }

    @Test
    public void testRemoveGroup() {
        ObservationHeading observationHeading = TestUtils.createObservationHeading("OBS1");
        observationHeading.setId(1L);
        Group group = TestUtils.createGroup("GROUP1");
        group.setId(2L);

        ObservationHeadingGroup observationHeadingGroup
                = new ObservationHeadingGroup(observationHeading, group, 3L, 4L);
        observationHeadingGroup.setId(3L);
        observationHeading.getObservationHeadingGroups().add(observationHeadingGroup);

        try {
            when(observationHeadingRepository.findOne(eq(observationHeading.getId()))).thenReturn(observationHeading);
            when(observationHeadingRepository.save(any(ObservationHeading.class))).thenReturn(observationHeading);
            when(groupRepository.findOne(eq(group.getId()))).thenReturn(group);

            mockMvc.perform(MockMvcRequestBuilders.delete("/observationheadinggroup/"
                    + observationHeadingGroup.getId()))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            verify(observationHeadingService, Mockito.times(1)).removeObservationHeadingGroup(eq(observationHeadingGroup.getId()));
        } catch (Exception e) {
            fail("Exception thrown" + e.getMessage());
        }
    }

    @Test
    public void testGetResultClusters() {
        try {
            mockMvc.perform(MockMvcRequestBuilders.get("/resultclusters")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        } catch (Exception e) {
            fail("Exception: " + e.getMessage());
        }
        verify(observationHeadingService, Mockito.times(1)).getResultClusters();
    }
}


