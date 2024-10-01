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

class Result {
  enum Type {
    OK, WARN, ERROR
  }

  static Result ok(String message) {
    return new Result(Type.OK, message, null)
  }

  static ResultBuilder warn(String message) {
    return new ResultBuilder(Type.WARN, message)
  }

  static ResultBuilder error(String message) {
    return new ResultBuilder(Type.ERROR, message)
  }

  private final Type type
  private final String message
  private final Closure<Void> fix

  Result(Type type, String message, Closure<Void> fix) {
    this.type = type
    this.message = message
    this.fix = fix
  }

  boolean isFixable() {
    return fix != null
  }

  Type getType() {
    return type
  }

  String getMessage() {
    return message
  }

  void fix() {
    if (fix != null) {
      fix.call()
    }
  }

  static class ResultBuilder {

    private final Type type
    private final String message
    private Closure<Void> fix

    ResultBuilder(Type type, String message) {
      this.type = type
      this.message = message
    }

    ResultBuilder withFix(Closure<Void> fix) {
      this.fix = fix
      return this
    }

    Result build() {
      return new Result(type, message, fix)
    }
  }
}
