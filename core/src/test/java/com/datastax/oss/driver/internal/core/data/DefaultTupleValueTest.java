/*
 * Copyright (C) 2017-2017 DataStax Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datastax.oss.driver.internal.core.data;

import com.datastax.oss.driver.api.core.data.TupleValue;
import com.datastax.oss.driver.api.core.detach.AttachmentPoint;
import com.datastax.oss.driver.api.core.type.DataType;
import com.datastax.oss.driver.api.core.type.DataTypes;
import com.datastax.oss.driver.internal.SerializationHelper;
import com.datastax.oss.driver.internal.core.type.DefaultTupleType;
import com.datastax.oss.protocol.internal.util.Bytes;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultTupleValueTest extends AccessibleByIndexTestBase<TupleValue> {

  @Override
  protected TupleValue newInstance(List<DataType> dataTypes, AttachmentPoint attachmentPoint) {
    DefaultTupleType type = new DefaultTupleType(dataTypes, attachmentPoint);
    return type.newValue();
  }

  @Test
  public void should_serialize_and_deserialize() {
    DefaultTupleType type =
        new DefaultTupleType(ImmutableList.of(DataTypes.INT, DataTypes.TEXT), attachmentPoint);
    TupleValue in = type.newValue();
    in.setBytesUnsafe(0, Bytes.fromHexString("0x00000001"));
    in.setBytesUnsafe(1, Bytes.fromHexString("0x61"));

    TupleValue out = SerializationHelper.serializeAndDeserialize(in);

    assertThat(out.getType()).isEqualTo(in.getType());
    assertThat(out.getType().isDetached()).isTrue();
    assertThat(Bytes.toHexString(out.getBytesUnsafe(0))).isEqualTo("0x00000001");
    assertThat(Bytes.toHexString(out.getBytesUnsafe(1))).isEqualTo("0x61");
  }
}
