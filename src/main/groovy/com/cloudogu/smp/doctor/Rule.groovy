package com.cloudogu.smp.doctor

interface Rule {
  Result validate(Context context)
}
