(ns eq-server.unit.models.user
  (:use [eq-server.models.user]
        [midje.sweet]))

(fact "crypted password matching works"
      (let [plain-pwd "SDFO8D**9"
            crypted-pwd (encrypt-pwd plain-pwd)]
        (pwd-match? plain-pwd crypted-pwd) => true))