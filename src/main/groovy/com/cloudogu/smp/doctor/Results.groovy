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
