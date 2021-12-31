package oikos.app.common.utils;

import oikos.app.common.models.BaseEntity;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.EntityPersister;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Mohamed Haamdi on 26/04/2021.
 */
class NanoIDGeneratorTest {
  @Test void generateEmailVerificationCode() {
    // when
    final var res = NanoIDGenerator.generateEmailVerificationCode();
    final var res2 = NanoIDGenerator.generateEmailVerificationCode();
    // then
    assertThat(res).hasSize(21).isNotEqualTo(res2);
  }

  @Test void generateSMSVerificationCode() {
    // when
    final var res = NanoIDGenerator.generateSMSVerificationCode();
    final var res2 = NanoIDGenerator.generateSMSVerificationCode();
    // then
    assertThat(res).hasSize(6).isNotEqualTo(res2).containsOnlyDigits();
  }

  @Test void generateID() {
    //when
    final var res = NanoIDGenerator.generateID();
    final var res2 = NanoIDGenerator.generateID();
    //then
    assertThat(res).hasSize(8).isNotEqualTo(res2);
  }

  @Test void generateIDwhenEntityDoesntHaveId() {
    // given
    NanoIDGenerator generator = new NanoIDGenerator();
    BaseEntity baseEntity = new BaseEntity();
    SharedSessionContractImplementor session =
      mock(SharedSessionContractImplementor.class);
    when(session.getEntityPersister(null, baseEntity))
      .thenReturn(mock(EntityPersister.class));
    when(session.getEntityPersister(null, baseEntity).getClassMetadata())
      .thenReturn(mock(ClassMetadata.class));
    // when
    String s = generator.generate(session, baseEntity);
    String s2 = generator.generate(session, baseEntity);
    // then
    assertThat(s).hasSize(NanoIDGenerator.NANOID_SIZE)
      .doesNotContainAnyWhitespaces().isNotEqualTo(s2);
  }

  @Test void generateIDwhenEntityDoesntHasId() {
    // given
    var presetID = "00000000";
    NanoIDGenerator generator = new NanoIDGenerator();
    BaseEntity baseEntity = new BaseEntity();
    baseEntity.setId(presetID);
    SharedSessionContractImplementor session =
      mock(SharedSessionContractImplementor.class);
    when(session.getEntityPersister(null, baseEntity))
      .thenReturn(mock(EntityPersister.class));
    when(session.getEntityPersister(null, baseEntity).getClassMetadata())
      .thenReturn(mock(ClassMetadata.class));
    when(session.getEntityPersister(null, baseEntity).getClassMetadata()
      .getIdentifier(baseEntity, session)).thenReturn(presetID);
    // when
    String s = generator.generate(session, baseEntity);
    // then
    assertThat(s).isEqualTo(presetID);
  }
}
