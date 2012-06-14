package nz.org.nesi.goldwrap.utils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanHelpers {

	public static Logger myLogger = LoggerFactory.getLogger(BeanHelpers.class);

	public static <M> void merge(M original, M propertiesToMerge)
			throws Exception {
		BeanInfo beanInfo = Introspector.getBeanInfo(propertiesToMerge
				.getClass());

		// Iterate over all the attributes
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {

			// Only copy writable attributes
			if (descriptor.getWriteMethod() != null) {
				Object newValue = descriptor.getReadMethod().invoke(
						propertiesToMerge);

				// Only copy values values where the destination values is null
				if (newValue != null) {
					if (original instanceof String) {
						if (StringUtils.isBlank((String) newValue)) {
							continue;
						}
					} else if (original instanceof Integer) {
						if ((Integer) original < 0) {
							continue;
						}
					} else if (original instanceof Long) {
						if ((Long) original < 0) {
							continue;
						}
					}

					myLogger.debug("Chaning property value: "
							+ newValue.toString());
					descriptor.getWriteMethod().invoke(original, newValue);
				}

			}
		}
	}
}
