package com.example.glukdataapp.realm;

import com.example.glukdataapp.realm.models.GlucoseEntry;
import com.example.glukdataapp.realm.models.IModel;
import com.example.glukdataapp.realm.models.InsulinEntry;

import java.util.List;
import java.util.logging.Logger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

public class RealmControl implements IRealmControl {

    Logger LOG = Logger.getLogger(RealmControl.class.getSimpleName());
    //Realm realm;
    RealmConfiguration config;

    public RealmControl() {
        config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
    }

    @Override
    public void clearRealm() {
        Realm realm = Realm.getInstance(config);
        try {
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.deleteAll();
                }
            });
        }
        catch(IllegalStateException ex){
            LOG.severe(ex.toString());
        }

    }

    @Override
    public boolean saveValue(final IModel item) {
        boolean success = false;
        Realm realm = Realm.getInstance(config);

        try {
            if (item instanceof GlucoseEntry) {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm((GlucoseEntry) item);
                    }
                });
                success = true;
            }
            if (item instanceof InsulinEntry) {
                realm.executeTransactionAsync(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        realm.copyToRealm((InsulinEntry) item);
                    }
                });

                success = true;
            }
        } catch (IllegalArgumentException ex) {
            LOG.severe(ex.toString());
            success = false;
        }
        finally {
            return success;
        }
    }

    @Override
    public List<GlucoseEntry> getGlucoseList() {
        Realm realm = Realm.getInstance(config);
        return realm.where(GlucoseEntry.class).findAll();
    }

    @Override
    public List<InsulinEntry> getInsulinList() {
        Realm realm = Realm.getInstance(config);
        return realm.where(InsulinEntry.class).findAll();
    }

    @Override
    public void deleteEntry(final IModel item) {
        Realm realm = Realm.getInstance(config);

        if(item instanceof GlucoseEntry){
            final RealmResults<GlucoseEntry> result = realm.where(GlucoseEntry.class).equalTo("_id", item.get_id()).findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    result.deleteAllFromRealm();
                }
            });

        }
        else if(item instanceof InsulinEntry){
            final RealmResults<InsulinEntry> result = realm.where(InsulinEntry.class).equalTo("_id", item.get_id()).findAll();
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    result.deleteAllFromRealm();
                }
            });
        }
    }


    @Override
    public int generateGlucoseId() {
        Realm realm = Realm.getInstance(config);
        Number last = realm.where(GlucoseEntry.class).max("_id");
        if(last == null) return 0;
        return last.intValue() + 1;
    }

    @Override
    public int generateInsulinId() {
        Realm realm = Realm.getInstance(config);
        Number last = realm.where(InsulinEntry.class).max("_id");
        if(last == null) return 0;
        return last.intValue() + 1;
    }

    @Override
    public void updateDate( IModel item, final int year, final int month, final int dayOfMonth) {
        Realm realm = Realm.getInstance(config);
        realm.beginTransaction();
        item.setDate(year, month, dayOfMonth);
        realm.commitTransaction();
    }

    @Override
    public void updateTime(IModel item, int hours, int mins) {
        Realm realm = Realm.getInstance(config);
        realm.beginTransaction();
        item.setTime(hours, mins);
        realm.commitTransaction();
    }

    @Override
    public void updateValue(IModel item, Float value) {

        try(Realm realm = Realm.getInstance(config)){
            realm.beginTransaction();
            item.setValue(value);
            realm.commitTransaction();
        }
    }

    @Override
    public void clearGlucoses() {
        try(Realm realm = Realm.getInstance(config)){
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(GlucoseEntry.class);
                }
            });
        }
    }

    @Override
    public void clearInsulins() {
        try(Realm realm = Realm.getInstance(config)){
            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    realm.delete(InsulinEntry.class);
                }
            });
        }
    }
}


