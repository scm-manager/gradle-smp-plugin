/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package com.cloudogu.smp.doctor

class Results implements Iterable<Result> {

  private final List<Result> resultList

  Results(List<Result> resultList) {
    this.resultList = resultList
  }

  static Results of(Result... results) {
    return new Results(Arrays.asList(results))
  }

  boolean hasError() {
    return resultList.any {result ->
      result.getType() == Result.Type.ERROR
    }
  }

  Results fixable() {
    List<Result> fixable = resultList.findAll { result ->
      return result.isFixable()
    }
    return new Results(fixable)
  }

  boolean isEmpty() {
    return resultList.isEmpty()
  }

  @Override
  Iterator<Result> iterator() {
    return resultList.iterator();
  }
}
