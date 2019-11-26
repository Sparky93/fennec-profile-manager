/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.fpm.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.recyclerview.widget.RecyclerView
import org.mozilla.fpm.R
import org.mozilla.fpm.models.Backup

class BackupsRVAdapter : RecyclerView.Adapter<BackupsRVAdapter.BackupViewHolder>() {

    private var dataSource: MutableList<Backup> = ArrayList()

    fun updateData(data: List<Backup>) {
        dataSource = data.toMutableList()
        this.notifyDataSetChanged()
    }

    fun add(backup: Backup) {
        dataSource.add(backup)
        this.notifyItemInserted(dataSource.size - 1)
    }

    override fun getItemCount(): Int {
        return dataSource.size
    }

    @NonNull
    override fun onCreateViewHolder(@NonNull viewGroup: ViewGroup, i: Int): BackupViewHolder {
        val view = LayoutInflater.from(viewGroup.context).inflate(R.layout.backup_row, viewGroup, false)
        return BackupViewHolder(view)
    }

    override fun onBindViewHolder(@NonNull backupViewHolder: BackupViewHolder, i: Int) {
        backupViewHolder.bind(i)
    }

    inner class BackupViewHolder(@NonNull itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.title)
        private val date: TextView = itemView.findViewById(R.id.date)

        fun bind(position: Int) {
            val backup: Backup = dataSource[position]
            title.text = backup.name
            date.text = backup.createdAt
        }
    }
}
