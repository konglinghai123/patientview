// Generated by CoffeeScript 1.6.3
(function() {
  var e, log, self;

  self = this;

  log = function(mess) {
    return plv8.elog(NOTICE, JSON.stringify(mess));
  };

  e = function() {
    return plv8.execute.apply(plv8, arguments);
  };

  this.sql = {
    log: function(mess) {
      return plv8.elog(NOTICE, JSON.stringify(mess));
    },
    insert_record: function(table_name, attrs) {
      return plv8.execute("insert into " + table_name + " (select * from json_populate_recordset(null::" + table_name + ", $1::json))", [JSON.stringify([attrs])]);
    },
    table_exists: function(table_name) {
      var _base;
      (_base = self.sql).exited_tables || (_base.exited_tables = plv8.execute("select table_name from information_schema.tables where table_schema = 'fhir'").map(function(i) {
        return i.table_name;
      }));
      return self.sql.exited_tables.indexOf(table_name) > -1;
    },
    columns: function(table_name) {
      var _base;
      (_base = self.sql).columns_for || (_base.columns_for = self.sql.collect_columns());
      return self.sql.columns_for[table_name];
    },
    resources: function(version) {
      return e("select * from meta.resources where version = $1", [version]);
    },
    collect_columns: function() {
      var cols;
      cols = plv8.execute("select table_name, column_name from information_schema.columns where table_schema = 'fhir'", []);
      return cols.reduce((function(acc, col) {
        acc[col.table_name] = acc[col.table_name] || [];
        acc[col.table_name].push(col);
        return acc;
      }), {});
    },
    collect_attributes: function(table_name, obj) {
      var arr2lit, columns;
      columns = self.sql.columns(table_name);
      arr2lit = function(v) {
        v = v.map(function(i) {
          return "\"" + i + "\"";
        }).join(',');
        return "{" + v + "}";
      };
      return columns.reduce((function(acc, m) {
        var an, col_prefix, column_name, parts, v;
        column_name = m.column_name;
        an = self.str.camelize(column_name);
        v = obj[an];
        parts = column_name.match(/(.*)_reference/);
        if (parts) {
          col_prefix = parts[1];
          if (v = obj[self.str.camelize(col_prefix)]) {
            acc[column_name] = v.reference;
            acc["" + col_prefix + "_display"] = v.display;
          }
        } else if (v) {
          if (Array.isArray(v)) {
            acc[column_name] = arr2lit(v);
          } else {
            acc[m.column_name] = v;
          }
        }
        return acc;
      }), {});
    },
    uuid: function() {
      var sql;
      sql = 'select uuid_generate_v4() as uuid';
      return plv8.execute(sql)[0]['uuid'];
    },
    insert_resource: function(json) {
      var resource_name, s, schema, sql, uuid;
      resource_name = json.resourceType;
      s = self.str;
      sql = self.sql;
      uuid = sql.uuid;
      schema = 'fhir.';
      return sql.walk([], s.underscore(resource_name), json, function(parents, name, obj) {
        var agg_key, attrs, parent_key, parents_prefix, table_name;
        parents_prefix = sql.table_name(parents);
        table_name = s.underscore(name);
        if (parents_prefix.length > 0) {
          table_name = parents_prefix + '_' + table_name;
        }
        if (sql.table_exists(table_name)) {
          attrs = sql.collect_attributes(table_name, obj);
          attrs.id || (attrs.id = uuid());
          if (parents.length > 0) {
            agg_key = parents[0].name + '_id';
            attrs[agg_key] = parents[0].meta;
          }
          if (parents.length > 1) {
            parent_key = parents_prefix + '_id';
            attrs[parent_key] = parents[parents.length - 1].meta;
          }
          sql.insert_record(schema + table_name, attrs);
          return attrs.id;
        } else {

        }
      });
    },
    table_name: function(parents) {
      return parents.map(function(i) {
        return self.str.underscore(i.name);
      }).join('_');
    },
    walk: function(parents, name, obj, cb) {
      var key, new_parents, res, value, _results;
      res = cb.call(self, parents, name, obj);
      new_parents = parents.concat({
        name: name,
        obj: obj,
        meta: res
      });
      _results = [];
      for (key in obj) {
        value = obj[key];
        if (self.u.isObject(value)) {
          _results.push(self.sql.walk(new_parents, key, value, cb));
        } else if (Array.isArray(value)) {
          _results.push(value.map(function(v) {
            if (self.u.isObject(v)) {
              return self.sql.walk(new_parents, key, v, cb);
            }
          }));
        } else {
          _results.push(void 0);
        }
      }
      return _results;
    }
  };

  this.u = {
    log: function(mess) {
      return plv8.elog(NOTICE, JSON.stringify(mess));
    },
    isObject: function(obj) {
      return Object.prototype.toString.call(obj) === "[object Object]";
    }
  };

  this.str = {
    underscore: function(str) {
      return str.replace(/([a-z\d])([A-Z]+)/g, "$1_$2").replace(/[-\s]+/g, "_").toLowerCase();
    },
    camelize: function(str) {
      return str.replace(/[-_\s]+(.)?/g, function(match, c) {
        if (c) {
          return c.toUpperCase();
        } else {
          return "";
        }
      });
    },
    is_vowel: function(char) {
      if (char.length === 1) {
        return /[aeiou]/.test(char);
      }
    },
    tabelize: function(str) {
      var s;
      s = self.str;
      return s.pluralize(s.underscore(str));
    },
    pluralize: function(str) {
      if (str.slice(-1) === "y") {
        if (self.str.is_vowel(str.charAt(str.length - 2))) {
          return str + "s";
        } else {
          return str.slice(0, -1) + "ies";
        }
      } else if (str.substring(str.length - 2) === "us") {
        return str.slice(0, -2) + "i";
      } else if (["ch", "sh"].indexOf(str.substring(str.length - 2)) !== -1 || ["x", "s"].indexOf(str.slice(-1)) !== -1) {
        return str + "es";
      } else {
        return str + "s";
      }
    }
  };

}).call(this);
