/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.ids.messaging.core.daps;

/**
 * Exception is thrown if errors happen in the DAPS service.
 */
public class DapsConnectionException extends DapsTokenManagerException {
    private static final long serialVersionUID = 42L;

    /**
     * Exception is thrown if communication to the DAPS fails.
     * For example, if the DAPS URL is incorrect or other
     * connection problems to the DAPS occur.
     *
     * @param message the error message to be included with the exception
     */
    public DapsConnectionException(final String message) {
        super(message);
    }
}
