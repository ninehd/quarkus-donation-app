package com.redhat.quarkus.donation.mapper;

import com.redhat.quarkus.donation.dto.DonationResultDTO;
import com.redhat.quarkus.donation.entity.Donation;
import com.redhat.quarkus.donation.utils.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

@Mapper(componentModel = MappingConstants.ComponentModel.JAKARTA_CDI)
public interface DonationResponseMapper {

    /**
     * Map donation to result DTO
     */
    @Mapping(target = "success", source = "success")
    @Mapping(target = "orderId", source = "donation.paypalInfo.orderId")
    @Mapping(target = "amount", source = "donation.amount")
    @Mapping(target = "maskedEmail", source = "donation", qualifiedByName = "maskEmail")
    @Mapping(target = "donorEmail", source = "donation.donorEmail")
    @Mapping(target = "reason", source = "donation.paypalInfo.errorMessage")
    DonationResultDTO toResult(Donation donation, boolean success);

    /**
     * Mask email for privacy
     */
    @Named("maskEmail")
    default String maskEmail(Donation donation) {
        if (donation == null) {
            return null;
        }
        String paypalEmail = donation.getPaypalInfo().getEmail();
        String emailToMask = paypalEmail != null ? paypalEmail : donation.getDonorEmail();
        return StringUtils.maskEmail(emailToMask);
    }
}