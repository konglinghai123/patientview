package org.patientview.api.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.patientview.api.annotation.GroupMemberOnly;
import org.patientview.api.controller.model.GroupStatisticTO;
import org.patientview.persistence.model.GroupStatistic;
import org.patientview.persistence.model.enums.Roles;
import org.patientview.persistence.model.enums.StatisticTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by james@solidstategroup.com
 * Created on 05/06/2014
 */
public class Util {

    private Util() {}

    /**
     * This is convert the Iterable<T> type passed for Spring DAO interface into
     * a more useful typed List.
     *
     * @param iterable
     * @param <T>
     * @return
     */
    public static <T> List<T> iterableToList(Iterable<T> iterable) {

        if (iterable == null) {
            return Collections.emptyList();
        }

        List<T> list = new ArrayList<T>();
        Iterator<T> lookupIterator = iterable.iterator();

        while (lookupIterator.hasNext()) {
            list.add(lookupIterator.next());
        }
        return list;

    }

    // Retrieve the list of Roles from the annotation.
    public static Roles[] getRoles(JoinPoint joinPoint) {
        final org.aspectj.lang.Signature signature = joinPoint.getStaticPart().getSignature();
        if (signature instanceof MethodSignature) {
            final MethodSignature ms = (MethodSignature) signature;

            for (Annotation annotation : ms.getMethod().getDeclaredAnnotations())
                if (annotation.annotationType() == GroupMemberOnly.class) {
                    return Util.getRolesFromAnnotation(annotation);
                }
        }
        return null;
    }

    public static Roles[] getRolesFromAnnotation(Annotation annotation) {
        Method[] methods = annotation.annotationType().getMethods();
        for (Method method : methods) {
            String name = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?> componentType = returnType.getComponentType();
            if (name.equals("roles") && returnType.isArray()
                    && Roles.class.isAssignableFrom(componentType)) {
                Roles[] features;
                try {
                    features = (Roles[]) (method.invoke(annotation, new Object[] {}));
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Error executing value() method in annotation.getClass().getCanonicalName()", e);
                }
                return features;
            }
        }
        throw new RuntimeException("No value() method returning a Roles[] roles was found in annotation "
                        + annotation.getClass().getCanonicalName());
    }

    // TODO sprint 3 included in the ENUM fix
    public static Collection<GroupStatisticTO> convertGroupStatistics(final List<GroupStatistic> groupStatistics) {

        Map<Date, GroupStatisticTO> groupStatisticTOs = new HashMap<>();
        GroupStatisticTO groupStatisticTO = new GroupStatisticTO();

        for (GroupStatistic groupStatistic : groupStatistics) {
            StatisticTypes statisticType = StatisticTypes.valueOf(groupStatistic.getStatisticType().getValue());

            switch (statisticType) {
                case ADD_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientAdds(groupStatistic.getValue());
                    break;
                case DELETE_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientDeletes(groupStatistic.getValue());
                    break;
                case IMPORT_COUNT:
                    groupStatisticTO.setCountOfImportLoads(groupStatistic.getValue());
                    break;
                case IMPORT_FAIL_COUNT:
                    groupStatisticTO.setCountOfImportFails(groupStatistic.getValue());
                    break;
                case LOGON_COUNT:
                    groupStatisticTO.setCountOfLogons(groupStatistic.getValue());
                    break;
                case UNIQUE_LOGON_COUNT:
                    groupStatisticTO.setCountOfUniqueLogons(groupStatistic.getValue());
                    break;
                case PASSWORD_CHANGE_COUNT:
                    groupStatisticTO.setCountOfPasswordChanges(groupStatistic.getValue());
                    break;
                case ACCOUNT_LOCKED_COUNT:
                    groupStatisticTO.setCountOfAccountLocks(groupStatistic.getValue());
                    break;
                case VIEW_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientViews(groupStatistic.getValue());
                    break;
                case PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatients(groupStatistic.getValue());
                    break;
                case REMOVE_PATIENT_COUNT:
                    groupStatisticTO.setCountOfPatientRemoves(groupStatistic.getValue());
                    break;

            }

        }
        return groupStatisticTOs.values();
    }


}

