// Copyright 2019 The Nomulus Authors. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package google.registry.model.transaction;


import com.google.appengine.api.utils.SystemProperty;
import com.google.appengine.api.utils.SystemProperty.Environment.Value;
import com.google.common.annotations.VisibleForTesting;
import google.registry.model.ofy.DatastoreTransactionManager;
import google.registry.persistence.DaggerPersistenceComponent;
import google.registry.tools.RegistryToolEnvironment;

/** Factory class to create {@link TransactionManager} instance. */
// TODO: Rename this to PersistenceFactory and move to persistence package.
public class TransactionManagerFactory {

  private static final TransactionManager TM = createTransactionManager();
  @VisibleForTesting static JpaTransactionManager jpaTm = createJpaTransactionManager();

  private TransactionManagerFactory() {}

  private static JpaTransactionManager createJpaTransactionManager() {
    if (isInAppEngine()) {
      return DaggerPersistenceComponent.create().appEngineJpaTransactionManager();
    } else if (RegistryToolEnvironment.isInRegistryTool()
        && RegistryToolEnvironment.isJpaTmEnabled()) {
      return DaggerPersistenceComponent.create().nomulusToolJpaTransactionManager();
    } else {
      return DummyJpaTransactionManager.create();
    }
  }

  private static TransactionManager createTransactionManager() {
    // TODO: Determine how to provision TransactionManager after the dual-write. During the
    // dual-write transitional phase, we need the TransactionManager for both Datastore and Cloud
    // SQL, and this method returns the one for Datastore.
    return new DatastoreTransactionManager(null);
  }

  /**
   * This function uses App Engine API to determine if the current runtime environment is App
   * Engine.
   *
   * @see <a
   *     href="https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/utils/SystemProperty">App
   *     Engine API public doc</a>
   */
  private static boolean isInAppEngine() {
    // SystemProperty.environment.value() returns null if the current runtime is local JVM
    return SystemProperty.environment.value() == Value.Production;
  }

  /** Returns {@link TransactionManager} instance. */
  public static TransactionManager tm() {
    return TM;
  }

  /** Returns {@link JpaTransactionManager} instance. */
  public static JpaTransactionManager jpaTm() {
    return jpaTm;
  }
}
